package io.github.alexzhirkevich.qrose.matrix.aztec

import io.github.alexzhirkevich.qrose.matrix.Matrix2D
import io.github.alexzhirkevich.qrose.matrix.common.GenericGF
import io.github.alexzhirkevich.qrose.matrix.common.ReedSolomonEncoder
import kotlin.math.min

public object AztecEncoder {

    public const val DEFAULT_EC_PERCENT: Int = 33
    public const val DEFAULT_LAYERS: Int = 0

    private val NB_BITS_COMPACT = intArrayOf(0, 104, 240, 408, 608)
    private val NB_CHUNKS_COMPACT = intArrayOf(0, 17, 40, 51, 76)
    private val WORD_SIZE_COMPACT = intArrayOf(4, 6, 6, 8, 8)

    public fun encode(
        data: String,
        minEccPercent: Int = DEFAULT_EC_PERCENT,
        userSpecifiedLayers: Int = DEFAULT_LAYERS
    ): Matrix2D {
        val bytes = data.encodeToByteArray()
        val bits = AztecHighLevelEncoder(bytes).encode()

        // 1. Bit stuffing
        var eccBits = (bits.size * minEccPercent / 100) + 11
        var totalSizeBits = bits.size + eccBits
        var layers: Int
        var wordSize: Int
        var totalBitsInSymbol: Int
        var compact: Boolean
        var stuffedBits: BooleanArrayList

        if (userSpecifiedLayers != DEFAULT_LAYERS) {
            compact = userSpecifiedLayers < 0
            layers = kotlin.math.abs(userSpecifiedLayers)
            if (layers > (if (compact) 4 else 32)) {
                throw IllegalArgumentException("Illegal layers: $userSpecifiedLayers")
            }
            totalBitsInSymbol = totalBitsInSymbol(layers, compact)
            wordSize = wordSize(layers, compact)
            val usableBits = totalBitsInSymbol - (totalBitsInSymbol % wordSize)
            stuffedBits = stuffBits(bits, wordSize)
            if (stuffedBits.size + eccBits > usableBits) {
                throw IllegalArgumentException("Data too large for specified status: $userSpecifiedLayers layers")
            }
            if (compact && stuffedBits.size > wordSize * NB_CHUNKS_COMPACT[layers]) {
                throw IllegalArgumentException("Data too large for compact $layers layers")
            }
        } else {
            wordSize = 0
            stuffedBits = BooleanArrayList()
            var i = 0
            while (true) {
                if (i > 32) {
                    throw IllegalArgumentException("Data too large for Aztec code")
                }
                compact = i <= 3
                layers = if (compact) i + 1 else i
                totalBitsInSymbol = totalBitsInSymbol(layers, compact)
                if (totalSizeBits > totalBitsInSymbol) {
                    i++
                    continue
                }
                if (wordSize != wordSize(layers, compact)) {
                    wordSize = wordSize(layers, compact)
                    stuffedBits = stuffBits(bits, wordSize)
                }
                val usableBits = totalBitsInSymbol - (totalBitsInSymbol % wordSize)
                if (compact && stuffedBits.size > wordSize * NB_CHUNKS_COMPACT[layers]) {
                    i++
                    continue
                }
                if (stuffedBits.size + eccBits <= usableBits) {
                    break
                }
                i++
            }
        }

        val messageBits = generateCheckWords(stuffedBits, totalBitsInSymbol, wordSize)
        val messageSizeInWords = stuffedBits.size / wordSize
        val modeMessage = generateModeMessage(compact, layers, messageSizeInWords)

        // Matrix drawing
        val baseMatrixSize = if (compact) 11 + layers * 4 else 14 + layers * 4
        val alignmentMap = IntArray(baseMatrixSize)
        val matrixSize: Int

        if (compact) {
            matrixSize = baseMatrixSize
            for (i in alignmentMap.indices) {
                alignmentMap[i] = i
            }
        } else {
            matrixSize = baseMatrixSize + 1 + 2 * ((baseMatrixSize / 2 - 1) / 15)
            val origCenter = baseMatrixSize / 2
            val center = matrixSize / 2
            for (i in 0 until origCenter) {
                val newOffset = i + i / 15
                alignmentMap[origCenter - i - 1] = center - newOffset - 1
                alignmentMap[origCenter + i] = center + newOffset + 1
            }
        }

        val matrix = Matrix2D(matrixSize, matrixSize)

        // Draw reference grid for full symbols
        if (!compact) {
            val center = matrixSize / 2
            for (i in -center..center) {
                for (j in -center..center) {
                    if (i % 16 == 0 || j % 16 == 0) {
                        matrix[center + i, center + j] = ((center + i) % 2 == 0) xor ((center + j) % 2 == 0)
                    }
                }
            }
        }

        // Draw bullseye
        drawBullsEye(matrix, matrixSize / 2, if (compact) 4 else 6)

        // Draw mode message
        drawModeMessage(matrix, compact, matrixSize, modeMessage, alignmentMap)

        // Draw data spirals
        drawDataSpiral(matrix, compact, layers, alignmentMap, messageBits)

        return matrix
    }

    private fun totalBitsInSymbol(layers: Int, compact: Boolean): Int {
        return if (compact) {
            NB_BITS_COMPACT[layers]
        } else {
            (112 + 16 * layers) * layers
        }
    }

    private fun wordSize(layers: Int, compact: Boolean): Int {
        return if (compact) {
            WORD_SIZE_COMPACT[layers]
        } else {
            when {
                layers <= 2 -> 6
                layers <= 8 -> 8
                layers <= 16 -> 10
                else -> 12
            }
        }
    }

    private fun stuffBits(bits: BooleanArrayList, wordSize: Int): BooleanArrayList {
        val out = BooleanArrayList()
        val n = bits.size
        val mask = (1 shl wordSize) - 2
        var i = 0
        while (i < n) {
            var value = 0
            for (j in 0 until wordSize) {
                if (i + j >= n || bits[i + j]) {
                    value = value or (1 shl (wordSize - 1 - j))
                }
            }
            if ((value and mask) == mask) {
                out.appendBits(value and mask, wordSize)
                i--
            } else if ((value and mask) == 0) {
                out.appendBits(value or 1, wordSize)
                i--
            } else {
                out.appendBits(value, wordSize)
            }
            i += wordSize
        }
        return out
    }

    private fun generateCheckWords(stuffedBits: BooleanArrayList, totalBitsInSymbol: Int, wordSize: Int): BooleanArrayList {
        val messageSizeInWords = stuffedBits.size / wordSize
        val totalWords = totalBitsInSymbol / wordSize
        val eccWords = totalWords - messageSizeInWords

        val gf = when (wordSize) {
            4 -> GenericGF.AZTEC_PARAM
            6 -> GenericGF.AZTEC_DATA_6
            8 -> GenericGF.AZTEC_DATA_8
            10 -> GenericGF.AZTEC_DATA_10
            12 -> GenericGF.AZTEC_DATA_12
            else -> throw IllegalArgumentException("Unsupported word size: $wordSize")
        }

        val rs = ReedSolomonEncoder(gf)
        val dataWords = IntArray(totalWords)
        for (i in 0 until messageSizeInWords) {
            var value = 0
            for (j in 0 until wordSize) {
                if (stuffedBits[i * wordSize + j]) {
                    value = value or (1 shl (wordSize - 1 - j))
                }
            }
            dataWords[i] = value
        }

        rs.encode(dataWords, eccWords)

        val startPad = totalBitsInSymbol % wordSize
        val messageBits = BooleanArrayList()
        messageBits.appendBits(0, startPad)
        for (word in dataWords) {
            messageBits.appendBits(word, wordSize)
        }
        return messageBits
    }

    private fun generateModeMessage(compact: Boolean, layers: Int, messageSizeInWords: Int): BooleanArrayList {
        val modeMessage = BooleanArrayList()
        if (compact) {
            modeMessage.appendBits(layers - 1, 2)
            modeMessage.appendBits(messageSizeInWords - 1, 6)
            return generateCheckWordsMode(modeMessage, 28, 4)
        } else {
            modeMessage.appendBits(layers - 1, 5)
            modeMessage.appendBits(messageSizeInWords - 1, 11)
            return generateCheckWordsMode(modeMessage, 40, 4)
        }
    }

    private fun generateCheckWordsMode(stuffedBits: BooleanArrayList, totalBits: Int, wordSize: Int): BooleanArrayList {
        val messageSizeInWords = stuffedBits.size / wordSize
        val totalWords = totalBits / wordSize
        val eccWords = totalWords - messageSizeInWords

        val rs = ReedSolomonEncoder(GenericGF.AZTEC_PARAM)
        val dataWords = IntArray(totalWords)
        for (i in 0 until messageSizeInWords) {
            var value = 0
            for (j in 0 until wordSize) {
                if (stuffedBits[i * wordSize + j]) {
                    value = value or (1 shl (wordSize - 1 - j))
                }
            }
            dataWords[i] = value
        }
        rs.encode(dataWords, eccWords)

        val messageBits = BooleanArrayList()
        for (word in dataWords) {
            messageBits.appendBits(word, wordSize)
        }
        return messageBits
    }

    private fun drawBullsEye(matrix: Matrix2D, center: Int, size: Int) {
        for (i in 0 until size step 2) {
            for (j in center - i..center + i) {
                matrix[j, center - i] = true
                matrix[j, center + i] = true
                matrix[center - i, j] = true
                matrix[center + i, j] = true
            }
        }
        matrix[center - size, center - size] = true
        matrix[center - size + 1, center - size] = true
        matrix[center - size, center - size + 1] = true
        matrix[center + size, center - size] = true
        matrix[center + size, center - size + 1] = true
        matrix[center + size, center + size - 1] = true
    }

    private fun drawModeMessage(
        matrix: Matrix2D,
        compact: Boolean,
        matrixSize: Int,
        modeMessage: BooleanArrayList,
        alignmentMap: IntArray
    ) {
        val center = matrixSize / 2
        if (compact) {
            for (i in 0 until 7) {
                val offset = center - 3 + i
                if (modeMessage[i]) matrix[alignmentMap[offset], alignmentMap[center - 5]] = true
                if (modeMessage[i + 7]) matrix[alignmentMap[center + 5], alignmentMap[offset]] = true
                if (modeMessage[i + 14]) matrix[alignmentMap[offset], alignmentMap[center + 5]] = true
                if (modeMessage[i + 21]) matrix[alignmentMap[center - 5], alignmentMap[offset]] = true
            }
        } else {
            for (i in 0 until 10) {
                val offset = center - 5 + i + i / 5
                if (modeMessage[i]) matrix[alignmentMap[offset], alignmentMap[center - 7]] = true
                if (modeMessage[i + 10]) matrix[alignmentMap[center + 7], alignmentMap[offset]] = true
                if (modeMessage[i + 20]) matrix[alignmentMap[offset], alignmentMap[center + 7]] = true
                if (modeMessage[i + 30]) matrix[alignmentMap[center - 7], alignmentMap[offset]] = true
            }
        }
    }

    private fun drawDataSpiral(
        matrix: Matrix2D,
        compact: Boolean,
        layers: Int,
        alignmentMap: IntArray,
        messageBits: BooleanArrayList
    ) {
        val center = alignmentMap.size / 2
        var rowOffset = 0
        for (i in 0 until layers) {
            val rowSize = (layers - i) * 4 + if (compact) 9 else 12
            for (j in 0 until rowSize) {
                val k = j * 2
                for (bit in 0 until 2) {
                    if (rowOffset + k + bit < messageBits.size && messageBits[rowOffset + k + bit]) {
                        matrix[alignmentMap[center - (layers - i) * 2 - (if (compact) 4 else 6) + bit], alignmentMap[center - (layers - i) * 2 - (if (compact) 4 else 6) + j]] = true
                    }
                    if (rowOffset + rowSize * 2 + k + bit < messageBits.size && messageBits[rowOffset + rowSize * 2 + k + bit]) {
                        matrix[alignmentMap[center - (layers - i) * 2 - (if (compact) 4 else 6) + j], alignmentMap[center + (layers - i) * 2 + (if (compact) 4 else 6) - 1 - bit]] = true
                    }
                    if (rowOffset + rowSize * 4 + k + bit < messageBits.size && messageBits[rowOffset + rowSize * 4 + k + bit]) {
                        matrix[alignmentMap[center + (layers - i) * 2 + (if (compact) 4 else 6) - 1 - bit], alignmentMap[center + (layers - i) * 2 + (if (compact) 4 else 6) - 1 - j]] = true
                    }
                    if (rowOffset + rowSize * 6 + k + bit < messageBits.size && messageBits[rowOffset + rowSize * 6 + k + bit]) {
                        matrix[alignmentMap[center + (layers - i) * 2 + (if (compact) 4 else 6) - 1 - j], alignmentMap[center - (layers - i) * 2 - (if (compact) 4 else 6) + bit]] = true
                    }
                }
            }
            rowOffset += rowSize * 8
        }
    }
}
