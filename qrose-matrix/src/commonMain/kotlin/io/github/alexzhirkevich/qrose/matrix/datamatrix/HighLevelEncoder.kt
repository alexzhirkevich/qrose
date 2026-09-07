package io.github.alexzhirkevich.qrose.matrix.datamatrix

import kotlin.math.ceil
import kotlin.math.min

internal object HighLevelEncoder {

    const val ASCII_ENCODATION = 0
    const val C40_ENCODATION = 1
    const val TEXT_ENCODATION = 2
    const val X12_ENCODATION = 3
    const val EDIFACT_ENCODATION = 4
    const val BASE256_ENCODATION = 5

    private const val PAD = 129
    private const val LATCH_TO_C40 = 230
    private const val LATCH_TO_BASE256 = 231
    private const val UPPER_SHIFT = 235
    private const val LATCH_TO_ANSIX12 = 238
    private const val LATCH_TO_TEXT = 239
    private const val LATCH_TO_EDIFACT = 240
    private const val C40_UNLATCH = 254
    private const val X12_UNLATCH = 254

    fun encodeHighLevel(msg: String, shape: DataMatrixShape): Pair<IntArray, SymbolInfo> {
        val context = EncoderContext(msg, shape)

        while (context.hasMoreCharacters()) {
            when (context.encodingMode) {
                ASCII_ENCODATION -> encodeAscii(context)
                C40_ENCODATION -> encodeC40(context)
                TEXT_ENCODATION -> encodeText(context)
                X12_ENCODATION -> encodeX12(context)
                EDIFACT_ENCODATION -> encodeEdifact(context)
                BASE256_ENCODATION -> encodeBase256(context)
                else -> throw IllegalStateException("Unknown mode: ${context.encodingMode}")
            }
        }

        val len = context.codewords.size
        context.updateSymbolInfo()
        val capacity = context.symbolInfo!!.dataCapacity
        if (len < capacity) {
            context.writeCodeword(PAD)
        }
        while (context.codewords.size < capacity) {
            val pad = randomize253State(PAD, context.codewords.size + 1)
            context.writeCodeword(pad)
        }

        return context.codewords.toIntArray() to context.symbolInfo!!
    }

    private fun randomize253State(codewordValue: Int, codewordPosition: Int): Int {
        val pseudoRandom = ((149 * codewordPosition) % 253) + 1
        val temp = codewordValue + pseudoRandom
        return if (temp <= 254) temp else temp - 254
    }

    private fun randomize255State(codewordValue: Int, codewordPosition: Int): Int {
        val pseudoRandom = ((149 * codewordPosition) % 255) + 1
        val temp = codewordValue + pseudoRandom
        return if (temp <= 255) temp else temp - 256
    }

    private fun encodeAscii(context: EncoderContext) {
        val n = lookAheadTest(context.msg, context.pos, ASCII_ENCODATION)
        if (n != ASCII_ENCODATION) {
            when (n) {
                C40_ENCODATION -> {
                    context.writeCodeword(LATCH_TO_C40)
                    context.encodingMode = C40_ENCODATION
                }
                TEXT_ENCODATION -> {
                    context.writeCodeword(LATCH_TO_TEXT)
                    context.encodingMode = TEXT_ENCODATION
                }
                X12_ENCODATION -> {
                    context.writeCodeword(LATCH_TO_ANSIX12)
                    context.encodingMode = X12_ENCODATION
                }
                EDIFACT_ENCODATION -> {
                    context.writeCodeword(LATCH_TO_EDIFACT)
                    context.encodingMode = EDIFACT_ENCODATION
                }
                BASE256_ENCODATION -> {
                    context.writeCodeword(LATCH_TO_BASE256)
                    context.encodingMode = BASE256_ENCODATION
                }
                else -> error("Illegal mode $n")
            }
        } else {
            val c = context.currentChar
            if (isDigit(c) && context.hasDigitAt(context.pos + 1)) {
                val d2 = context.msg[context.pos + 1]
                val num = (c - '0') * 10 + (d2 - '0')
                context.writeCodeword(num + 130)
                context.pos += 2
            } else {
                if (c.code in 1..127) {
                    context.writeCodeword(c.code + 1)
                    context.pos++
                } else if (c.code > 127) {
                    context.writeCodeword(UPPER_SHIFT)
                    context.writeCodeword((c.code - 128) + 1)
                    context.pos++
                } else {
                    context.writeCodeword(c.code + 1)
                    context.pos++
                }
            }
        }
    }

    private fun encodeC40(context: EncoderContext) {
        val buffer = StringBuilder()
        while (context.hasMoreCharacters()) {
            val c = context.currentChar
            context.pos++
            val lastChar = !context.hasMoreCharacters()
            encodeC40Char(c, buffer)

            val count = (buffer.length / 3) * 2
            context.updateSymbolInfo(context.codewords.size + count)
            val available = context.symbolInfo!!.dataCapacity - context.codewords.size

            if (lastChar) {
                writeC40Buffer(context, buffer)
                return
            }

            if (buffer.length % 3 == 0) {
                val newMode = lookAheadTest(context.msg, context.pos, C40_ENCODATION)
                if (newMode != C40_ENCODATION) {
                    writeC40Buffer(context, buffer)
                    context.writeCodeword(C40_UNLATCH)
                    context.encodingMode = ASCII_ENCODATION
                    return
                }
            }
        }
        writeC40Buffer(context, buffer)
    }

    private fun encodeC40Char(c: Char, buffer: StringBuilder) {
        when {
            c == ' ' -> buffer.append(3.toChar())
            c in '0'..'9' -> buffer.append((c - '0' + 4).toChar())
            c in 'A'..'Z' -> buffer.append((c - 'A' + 14).toChar())
            c.code < 32 -> {
                buffer.append(0.toChar())
                buffer.append(c)
            }
            c.code in 33..47 -> {
                buffer.append(1.toChar())
                buffer.append((c.code - 33).toChar())
            }
            c.code in 58..64 -> {
                buffer.append(1.toChar())
                buffer.append((c.code - 58 + 15).toChar())
            }
            c.code in 91..95 -> {
                buffer.append(1.toChar())
                buffer.append((c.code - 91 + 22).toChar())
            }
            c.code in 96..127 -> {
                buffer.append(2.toChar())
                buffer.append((c.code - 96).toChar())
            }
            else -> {
                buffer.append(1.toChar())
                buffer.append(0x1E.toChar())
                encodeC40Char((c.code - 128).toChar(), buffer)
            }
        }
    }

    private fun writeC40Buffer(context: EncoderContext, buffer: StringBuilder) {
        var idx = 0
        while (idx + 3 <= buffer.length) {
            val v1 = buffer[idx].code
            val v2 = buffer[idx + 1].code
            val v3 = buffer[idx + 2].code
            val value = 1600 * v1 + 40 * v2 + v3 + 1
            context.writeCodeword(value / 256)
            context.writeCodeword(value % 256)
            idx += 3
        }
        val rest = buffer.length - idx
        if (rest > 0) {
            if (context.encodingMode != ASCII_ENCODATION) {
                context.writeCodeword(C40_UNLATCH)
                context.encodingMode = ASCII_ENCODATION
            }
            context.pos -= rest
        }
    }

    private fun encodeText(context: EncoderContext) {
        val buffer = StringBuilder()
        while (context.hasMoreCharacters()) {
            val c = context.currentChar
            context.pos++
            val lastChar = !context.hasMoreCharacters()
            encodeTextChar(c, buffer)

            val count = (buffer.length / 3) * 2
            context.updateSymbolInfo(context.codewords.size + count)

            if (lastChar) {
                writeTextBuffer(context, buffer)
                return
            }

            if (buffer.length % 3 == 0) {
                val newMode = lookAheadTest(context.msg, context.pos, TEXT_ENCODATION)
                if (newMode != TEXT_ENCODATION) {
                    writeTextBuffer(context, buffer)
                    context.writeCodeword(C40_UNLATCH)
                    context.encodingMode = ASCII_ENCODATION
                    return
                }
            }
        }
        writeTextBuffer(context, buffer)
    }

    private fun encodeTextChar(c: Char, buffer: StringBuilder) {
        when {
            c == ' ' -> buffer.append(3.toChar())
            c in '0'..'9' -> buffer.append((c - '0' + 4).toChar())
            c in 'a'..'z' -> buffer.append((c - 'a' + 14).toChar())
            c.code < 32 -> {
                buffer.append(0.toChar())
                buffer.append(c)
            }
            c.code in 33..47 -> {
                buffer.append(1.toChar())
                buffer.append((c.code - 33).toChar())
            }
            c.code in 58..64 -> {
                buffer.append(1.toChar())
                buffer.append((c.code - 58 + 15).toChar())
            }
            c.code in 91..95 -> {
                buffer.append(1.toChar())
                buffer.append((c.code - 91 + 22).toChar())
            }
            c in 'A'..'Z' -> {
                buffer.append(2.toChar())
                buffer.append((c - 'A').toChar())
            }
            c.code in 123..127 -> {
                buffer.append(2.toChar())
                buffer.append((c.code - 123 + 27).toChar())
            }
            else -> {
                buffer.append(1.toChar())
                buffer.append(0x1E.toChar())
                encodeTextChar((c.code - 128).toChar(), buffer)
            }
        }
    }

    private fun writeTextBuffer(context: EncoderContext, buffer: StringBuilder) {
        var idx = 0
        while (idx + 3 <= buffer.length) {
            val v1 = buffer[idx].code
            val v2 = buffer[idx + 1].code
            val v3 = buffer[idx + 2].code
            val value = 1600 * v1 + 40 * v2 + v3 + 1
            context.writeCodeword(value / 256)
            context.writeCodeword(value % 256)
            idx += 3
        }
        val rest = buffer.length - idx
        if (rest > 0) {
            if (context.encodingMode != ASCII_ENCODATION) {
                context.writeCodeword(C40_UNLATCH)
                context.encodingMode = ASCII_ENCODATION
            }
            context.pos -= rest
        }
    }

    private fun encodeX12(context: EncoderContext) {
        val buffer = StringBuilder()
        while (context.hasMoreCharacters()) {
            val c = context.currentChar
            context.pos++
            encodeX12Char(c, buffer)
            if (buffer.length % 3 == 0) {
                var idx = 0
                while (idx + 3 <= buffer.length) {
                    val v1 = buffer[idx].code
                    val v2 = buffer[idx + 1].code
                    val v3 = buffer[idx + 2].code
                    val value = 1600 * v1 + 40 * v2 + v3 + 1
                    context.writeCodeword(value / 256)
                    context.writeCodeword(value % 256)
                    idx += 3
                }
                buffer.clear()
                val newMode = lookAheadTest(context.msg, context.pos, X12_ENCODATION)
                if (newMode != X12_ENCODATION) {
                    context.writeCodeword(X12_UNLATCH)
                    context.encodingMode = ASCII_ENCODATION
                    return
                }
            }
        }
        if (buffer.isNotEmpty()) {
            context.writeCodeword(X12_UNLATCH)
            context.encodingMode = ASCII_ENCODATION
            context.pos -= buffer.length
        }
    }

    private fun encodeX12Char(c: Char, buffer: StringBuilder) {
        when {
            c == '\r' -> buffer.append(0.toChar())
            c == '*' -> buffer.append(1.toChar())
            c == '>' -> buffer.append(2.toChar())
            c == ' ' -> buffer.append(3.toChar())
            c in '0'..'9' -> buffer.append((c - '0' + 4).toChar())
            c in 'A'..'Z' -> buffer.append((c - 'A' + 14).toChar())
            else -> throw IllegalArgumentException("Illegal character in X12: $c")
        }
    }

    private fun encodeEdifact(context: EncoderContext) {
        val buffer = StringBuilder()
        while (context.hasMoreCharacters()) {
            val c = context.currentChar
            if (c.code in 32..94) {
                buffer.append((c.code - 32).toChar())
                context.pos++
            } else {
                break
            }
            if (buffer.length == 4) {
                writeEdifactBlock(context, buffer)
                buffer.clear()
                val newMode = lookAheadTest(context.msg, context.pos, EDIFACT_ENCODATION)
                if (newMode != EDIFACT_ENCODATION) {
                    context.encodingMode = ASCII_ENCODATION
                    break
                }
            }
        }
        if (buffer.isNotEmpty()) {
            buffer.append(31.toChar()) // Unlatch
            while (buffer.length < 4) {
                buffer.append(0.toChar())
            }
            writeEdifactBlock(context, buffer)
            context.encodingMode = ASCII_ENCODATION
        } else if (context.encodingMode == EDIFACT_ENCODATION) {
            context.writeCodeword(0x1F) // unlatch
            context.encodingMode = ASCII_ENCODATION
        }
    }

    private fun writeEdifactBlock(context: EncoderContext, buffer: StringBuilder) {
        val v1 = buffer[0].code
        val v2 = buffer[1].code
        val v3 = buffer[2].code
        val v4 = buffer[3].code
        val value = (v1 shl 18) or (v2 shl 12) or (v3 shl 6) or v4
        context.writeCodeword((value shr 16) and 0xFF)
        context.writeCodeword((value shr 8) and 0xFF)
        context.writeCodeword(value and 0xFF)
    }

    private fun encodeBase256(context: EncoderContext) {
        val buffer = mutableListOf<Int>()
        buffer.add(0) // placeholder for length
        while (context.hasMoreCharacters()) {
            val c = context.currentChar
            buffer.add(c.code and 0xFF)
            context.pos++
            val newMode = lookAheadTest(context.msg, context.pos, BASE256_ENCODATION)
            if (newMode != BASE256_ENCODATION) {
                context.encodingMode = ASCII_ENCODATION
                break
            }
        }
        val dataCount = buffer.size - 1
        var lengthFieldSize = 1
        if (dataCount > 249) {
            lengthFieldSize = 2
            buffer[0] = (dataCount / 250 + 249)
            buffer.add(1, dataCount % 250)
        } else {
            buffer[0] = dataCount
        }

        for (i in 0 until buffer.size) {
            val cw = randomize255State(buffer[i], context.codewords.size + 1)
            context.writeCodeword(cw)
        }
    }

    private fun lookAheadTest(msg: String, startpos: Int, currentMode: Int): Int {
        if (startpos >= msg.length) return currentMode

        var charCounts = floatArrayOf(0f, 1f, 1f, 1f, 1f, 1.25f)
        when (currentMode) {
            C40_ENCODATION -> charCounts[C40_ENCODATION] = 0f
            TEXT_ENCODATION -> charCounts[TEXT_ENCODATION] = 0f
            X12_ENCODATION -> charCounts[X12_ENCODATION] = 0f
            EDIFACT_ENCODATION -> charCounts[EDIFACT_ENCODATION] = 0f
            BASE256_ENCODATION -> charCounts[BASE256_ENCODATION] = 0f
        }

        var charsScanned = 0
        while (startpos + charsScanned < msg.length) {
            val c = msg[startpos + charsScanned]
            charsScanned++

            // ASCII
            if (isDigit(c)) {
                charCounts[ASCII_ENCODATION] += 0.5f
            } else if (c.code > 127) {
                charCounts[ASCII_ENCODATION] = ceil(charCounts[ASCII_ENCODATION]) + 2f
            } else {
                charCounts[ASCII_ENCODATION] = ceil(charCounts[ASCII_ENCODATION]) + 1f
            }

            // C40
            if (c in '0'..'9' || c in 'A'..'Z' || c == ' ') {
                charCounts[C40_ENCODATION] += 2f / 3f
            } else if (c.code > 127) {
                charCounts[C40_ENCODATION] += 8f / 3f
            } else {
                charCounts[C40_ENCODATION] += 4f / 3f
            }

            // TEXT
            if (c in '0'..'9' || c in 'a'..'z' || c == ' ') {
                charCounts[TEXT_ENCODATION] += 2f / 3f
            } else if (c.code > 127) {
                charCounts[TEXT_ENCODATION] += 8f / 3f
            } else {
                charCounts[TEXT_ENCODATION] += 4f / 3f
            }

            // X12
            if (c in '0'..'9' || c in 'A'..'Z' || c == ' ' || c == '\r' || c == '*' || c == '>') {
                charCounts[X12_ENCODATION] += 2f / 3f
            } else {
                charCounts[X12_ENCODATION] = Float.MAX_VALUE / 2
            }

            // EDIFACT
            if (c.code in 32..94) {
                charCounts[EDIFACT_ENCODATION] += 3f / 4f
            } else {
                charCounts[EDIFACT_ENCODATION] = Float.MAX_VALUE / 2
            }

            // BASE256
            charCounts[BASE256_ENCODATION] += 1f

            if (charsScanned >= 4) {
                val minCount = charCounts.minOrNull() ?: Float.MAX_VALUE
                if (charCounts[ASCII_ENCODATION] == minCount) return ASCII_ENCODATION
                if (charCounts[BASE256_ENCODATION] == minCount) return BASE256_ENCODATION
                if (charCounts[EDIFACT_ENCODATION] == minCount) return EDIFACT_ENCODATION
                if (charCounts[TEXT_ENCODATION] == minCount) return TEXT_ENCODATION
                if (charCounts[X12_ENCODATION] == minCount) return X12_ENCODATION
                if (charCounts[C40_ENCODATION] == minCount) return C40_ENCODATION
            }
        }

        val minCount = charCounts.minOrNull() ?: Float.MAX_VALUE
        if (charCounts[ASCII_ENCODATION] == minCount) return ASCII_ENCODATION
        if (charCounts[BASE256_ENCODATION] == minCount) return BASE256_ENCODATION
        if (charCounts[EDIFACT_ENCODATION] == minCount) return EDIFACT_ENCODATION
        if (charCounts[TEXT_ENCODATION] == minCount) return TEXT_ENCODATION
        if (charCounts[X12_ENCODATION] == minCount) return X12_ENCODATION
        return C40_ENCODATION
    }

    private fun isDigit(c: Char): Boolean = c in '0'..'9'

    internal class EncoderContext(
        val msg: String,
        val shape: DataMatrixShape
    ) {
        var pos = 0
        var encodingMode = ASCII_ENCODATION
        val codewords = mutableListOf<Int>()
        var symbolInfo: SymbolInfo? = null

        val currentChar: Char
            get() = msg[pos]

        fun hasMoreCharacters(): Boolean = pos < msg.length

        fun hasDigitAt(index: Int): Boolean = index < msg.length && isDigit(msg[index])

        fun writeCodeword(cw: Int) {
            codewords.add(cw)
        }

        fun updateSymbolInfo(len: Int = codewords.size) {
            if (symbolInfo == null || len > symbolInfo!!.dataCapacity) {
                symbolInfo = SymbolInfo.lookup(len, shape)
            }
        }
    }
}
