package io.github.alexzhirkevich.qrose.matrix.pdf417

import io.github.alexzhirkevich.qrose.matrix.Matrix2D
import kotlin.math.abs

/**
 * PDF417 2D Barcode encoder according to ISO/IEC 15438:2001.
 */
internal object Pdf417Encoder {

    private const val PREFERRED_RATIO = 3.0f
    private const val DEFAULT_MODULE_WIDTH = 0.357f
    private const val HEIGHT = 2.0f

    /**
     * Encodes a string message into a [Matrix2D] containing the PDF417 barcode.
     *
     * @param data the message to encode
     * @param errorCorrectionLevel desired error correction level, or [Pdf417ErrorCorrectionLevel.Auto]
     * @param compaction compaction mode, or [Pdf417Compaction.Auto]
     * @param dimensions column and row bounds
     * @param compact if true, generates a Compact PDF417 (truncated stop pattern and no right row indicators)
     */
    fun encode(
        data: String,
        errorCorrectionLevel: Pdf417ErrorCorrectionLevel = Pdf417ErrorCorrectionLevel.Auto,
        compaction: Pdf417Compaction = Pdf417Compaction.Auto,
        dimensions: Pdf417Dimensions = Pdf417Dimensions(),
        compact: Boolean = false,
    ): Matrix2D {
        require(data.isNotEmpty()) { "Data to encode cannot be empty" }

        // Step 1: High-level encoding
        val highLevel = Pdf417HighLevelEncoder.encodeHighLevel(data, compaction)
        val sourceCodewordsCount = highLevel.size

        val ecLevel = if (errorCorrectionLevel == Pdf417ErrorCorrectionLevel.Auto) {
            Pdf417ErrorCorrection.getRecommendedMinimumErrorCorrectionLevel(sourceCodewordsCount)
        } else {
            errorCorrectionLevel.level
        }

        val ecCodewordsCount = Pdf417ErrorCorrection.getErrorCorrectionCodewordCount(ecLevel)

        // Step 2: Determine dimensions
        val (cols, rows) = determineDimensions(
            dimensions = dimensions,
            sourceCodewords = sourceCodewordsCount,
            errorCorrectionCodewords = ecCodewordsCount
        )

        val pad = getNumberOfPadCodewords(sourceCodewordsCount, ecCodewordsCount, cols, rows)

        if (sourceCodewordsCount + ecCodewordsCount + 1 > 929) {
            throw IllegalArgumentException("Encoded message contains too many codewords ($sourceCodewordsCount data + $ecCodewordsCount EC > 928)")
        }

        // Step 3: Construct data codewords with symbol length descriptor and padding
        val n = sourceCodewordsCount + pad + 1
        val dataCodewords = IntArray(n)
        dataCodewords[0] = n
        for (i in 0 until sourceCodewordsCount) {
            dataCodewords[i + 1] = highLevel[i]
        }
        for (i in (sourceCodewordsCount + 1) until n) {
            dataCodewords[i] = 900 // PAD codeword
        }

        // Step 4: Generate error correction codewords
        val ecCodewords = Pdf417ErrorCorrection.generateErrorCorrection(dataCodewords, ecLevel)

        val fullCodewords = IntArray(cols * rows)
        dataCodewords.copyInto(fullCodewords, 0, 0, dataCodewords.size)
        ecCodewords.copyInto(fullCodewords, dataCodewords.size, 0, ecCodewords.size)

        // Step 5: Low-level matrix layout
        val width = if (compact) 17 * cols + 35 else 17 * cols + 69
        val matrix = Matrix2D(width, rows)

        var idx = 0
        for (y in 0 until rows) {
            var col = 0
            val cluster = y % 3

            // Start pattern (17 bits)
            for (bit in 16 downTo 0) {
                matrix[col++, y] = ((Pdf417Codewords.START_PATTERN shr bit) and 1) != 0
            }

            // Left indicator codeword
            val left = when (cluster) {
                0 -> (30 * (y / 3)) + ((rows - 1) / 3)
                1 -> (30 * (y / 3)) + (ecLevel * 3) + ((rows - 1) % 3)
                else -> (30 * (y / 3)) + (cols - 1)
            }
            val leftPattern = Pdf417Codewords.CODEWORD_TABLE[cluster][left]
            for (bit in 16 downTo 0) {
                matrix[col++, y] = ((leftPattern shr bit) and 1) != 0
            }

            // Data columns
            for (x in 0 until cols) {
                val cw = fullCodewords[idx++]
                val dataPattern = Pdf417Codewords.CODEWORD_TABLE[cluster][cw]
                for (bit in 16 downTo 0) {
                    matrix[col++, y] = ((dataPattern shr bit) and 1) != 0
                }
            }

            if (compact) {
                // Truncated stop pattern: 1 single black bar
                matrix[col++, y] = true
            } else {
                // Right indicator codeword
                val right = when (cluster) {
                    0 -> (30 * (y / 3)) + (cols - 1)
                    1 -> (30 * (y / 3)) + ((rows - 1) / 3)
                    else -> (30 * (y / 3)) + (ecLevel * 3) + ((rows - 1) % 3)
                }
                val rightPattern = Pdf417Codewords.CODEWORD_TABLE[cluster][right]
                for (bit in 16 downTo 0) {
                    matrix[col++, y] = ((rightPattern shr bit) and 1) != 0
                }

                // Stop pattern (18 bits)
                for (bit in 17 downTo 0) {
                    matrix[col++, y] = ((Pdf417Codewords.STOP_PATTERN shr bit) and 1) != 0
                }
            }
        }

        return matrix
    }

    private fun calculateNumberOfRows(m: Int, k: Int, c: Int): Int {
        var r = ((m + 1 + k) / c) + 1
        if (c * r >= (m + 1 + k + c)) {
            r--
        }
        return r
    }

    private fun getNumberOfPadCodewords(m: Int, k: Int, c: Int, r: Int): Int {
        val n = c * r - k
        return if (n > m + 1) n - m - 1 else 0
    }

    private fun determineDimensions(
        dimensions: Pdf417Dimensions,
        sourceCodewords: Int,
        errorCorrectionCodewords: Int
    ): Pair<Int, Int> {
        var ratio = 0.0f
        var bestDimension: Pair<Int, Int>? = null
        var currentCol = dimensions.minCols

        for (cols in dimensions.minCols..dimensions.maxCols) {
            currentCol = cols
            val rows = calculateNumberOfRows(sourceCodewords, errorCorrectionCodewords, cols)
            if (rows < dimensions.minRows) {
                break
            }
            if (rows > dimensions.maxRows) {
                continue
            }
            val newRatio = ((17f * cols + 69f) * DEFAULT_MODULE_WIDTH) / (rows * HEIGHT)
            if (bestDimension != null && abs(newRatio - PREFERRED_RATIO) > abs(ratio - PREFERRED_RATIO)) {
                continue
            }
            ratio = newRatio
            bestDimension = cols to rows
        }

        if (bestDimension == null) {
            val rows = calculateNumberOfRows(sourceCodewords, errorCorrectionCodewords, currentCol)
            if (rows < dimensions.minRows) {
                bestDimension = dimensions.minCols to dimensions.minRows
            }
        }

        return bestDimension ?: throw IllegalArgumentException("Unable to fit message in columns")
    }
}
