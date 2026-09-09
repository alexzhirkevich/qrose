package io.github.alexzhirkevich.qrose.matrix.pdf417

import kotlin.math.min

/**
 * High-level encoder for PDF417 barcodes according to ISO/IEC 15438.
 * Converts input text or byte data into a sequence of codewords (0..928).
 */
internal object Pdf417HighLevelEncoder {

    private const val TEXT_COMPACTION = 0
    private const val BYTE_COMPACTION = 1
    private const val NUMERIC_COMPACTION = 2

    private const val SUBMODE_ALPHA = 0
    private const val SUBMODE_LOWER = 1
    private const val SUBMODE_MIXED = 2
    private const val SUBMODE_PUNCTUATION = 3

    private const val LATCH_TO_TEXT = 900
    private const val LATCH_TO_BYTE_PADDED = 901
    private const val LATCH_TO_NUMERIC = 902
    private const val SHIFT_TO_BYTE = 913
    private const val LATCH_TO_BYTE = 924
    private const val ECI_USER_DEFINED = 925
    private const val ECI_GENERAL_PURPOSE = 926
    private const val ECI_CHARSET = 927

    private val TEXT_MIXED_RAW = byteArrayOf(
        48, 49, 50, 51, 52, 53, 54, 55, 56, 57, 38, 13, 9, 44, 58,
        35, 45, 46, 36, 47, 43, 37, 42, 61, 94, 0, 32, 0, 0, 0
    )

    private val TEXT_PUNCTUATION_RAW = byteArrayOf(
        59, 60, 62, 64, 91, 92, 93, 95, 96, 126, 33, 13, 9, 44, 58,
        10, 45, 46, 36, 47, 34, 124, 42, 40, 41, 63, 123, 125, 39, 0
    )

    private val MIXED = ByteArray(128) { -1 }
    private val PUNCTUATION = ByteArray(128) { -1 }

    init {
        for (i in TEXT_MIXED_RAW.indices) {
            val b = TEXT_MIXED_RAW[i].toInt()
            if (b > 0) {
                MIXED[b] = i.toByte()
            }
        }
        for (i in TEXT_PUNCTUATION_RAW.indices) {
            val b = TEXT_PUNCTUATION_RAW[i].toInt()
            if (b > 0) {
                PUNCTUATION[b] = i.toByte()
            }
        }
    }

    /**
     * Encodes the message into PDF417 codewords using the given compaction mode.
     */
    fun encodeHighLevel(msg: String, compaction: Pdf417Compaction = Pdf417Compaction.Auto): IntArray {
        require(msg.isNotEmpty()) { "Message cannot be empty" }

        val target = mutableListOf<Int>()

        when (compaction) {
            Pdf417Compaction.Text -> {
                for (i in msg.indices) {
                    require(msg[i].code <= 127) {
                        "Non-encodable character '${msg[i]}' (code ${msg[i].code}) in Text compaction mode"
                    }
                }
                encodeText(msg, 0, msg.length, target, SUBMODE_ALPHA)
            }
            Pdf417Compaction.Numeric -> {
                for (i in msg.indices) {
                    require(isDigit(msg[i])) {
                        "Non-digit character '${msg[i]}' in Numeric compaction mode"
                    }
                }
                target.add(LATCH_TO_NUMERIC)
                encodeNumeric(msg, 0, msg.length, target)
            }
            Pdf417Compaction.Byte -> {
                val hasNonAscii = msg.any { it.code > 127 }
                if (hasNonAscii) {
                    encodingECI(26, target) // UTF-8 ECI
                }
                val bytes = msg.encodeToByteArray()
                encodeBinary(bytes, 0, bytes.size, BYTE_COMPACTION, target)
            }
            Pdf417Compaction.Auto -> {
                val hasNonAscii = msg.any { it.code > 127 }
                if (hasNonAscii) {
                    // When non-ASCII characters are present, encode full message as UTF-8 in Byte mode
                    encodingECI(26, target)
                    val bytes = msg.encodeToByteArray()
                    encodeBinary(bytes, 0, bytes.size, BYTE_COMPACTION, target)
                } else {
                    encodeAutoAscii(msg, target)
                }
            }
        }

        return target.toIntArray()
    }

    private fun encodeAutoAscii(msg: String, target: MutableList<Int>) {
        val len = msg.length
        var p = 0
        var encodingMode = TEXT_COMPACTION
        var textSubMode = SUBMODE_ALPHA

        while (p < len) {
            val n = determineConsecutiveDigitCount(msg, p)
            if (n >= 13) {
                target.add(LATCH_TO_NUMERIC)
                encodingMode = NUMERIC_COMPACTION
                textSubMode = SUBMODE_ALPHA
                encodeNumeric(msg, p, n, target)
                p += n
            } else {
                val t = determineConsecutiveTextCount(msg, p)
                if (t >= 5 || n == len - p) {
                    if (encodingMode != TEXT_COMPACTION) {
                        target.add(LATCH_TO_TEXT)
                        encodingMode = TEXT_COMPACTION
                        textSubMode = SUBMODE_ALPHA
                    }
                    textSubMode = encodeText(msg, p, t, target, textSubMode)
                    p += t
                } else {
                    var b = determineConsecutiveBinaryCount(msg, p)
                    if (b == 0) {
                        b = 1
                    }
                    val sub = msg.substring(p, p + b)
                    val bytes = sub.encodeToByteArray()
                    if (bytes.size == 1 && encodingMode == TEXT_COMPACTION) {
                        encodeBinary(bytes, 0, 1, TEXT_COMPACTION, target)
                    } else {
                        encodeBinary(bytes, 0, bytes.size, encodingMode, target)
                        encodingMode = BYTE_COMPACTION
                        textSubMode = SUBMODE_ALPHA
                    }
                    p += b
                }
            }
        }
    }

    private fun encodeText(
        msg: String,
        startpos: Int,
        count: Int,
        target: MutableList<Int>,
        initialSubmode: Int
    ): Int {
        val tmp = mutableListOf<Int>()
        var submode = initialSubmode
        var idx = 0

        while (idx < count) {
            val ch = msg[startpos + idx]
            when (submode) {
                SUBMODE_ALPHA -> {
                    if (isAlphaUpper(ch)) {
                        if (ch == ' ') {
                            tmp.add(26)
                        } else {
                            tmp.add(ch.code - 65)
                        }
                    } else {
                        if (isAlphaLower(ch)) {
                            submode = SUBMODE_LOWER
                            tmp.add(27) // ll
                            continue
                        } else if (isMixed(ch)) {
                            submode = SUBMODE_MIXED
                            tmp.add(28) // ml
                            continue
                        } else {
                            tmp.add(29) // ps
                            tmp.add(PUNCTUATION[ch.code].toInt())
                        }
                    }
                }
                SUBMODE_LOWER -> {
                    if (isAlphaLower(ch)) {
                        if (ch == ' ') {
                            tmp.add(26)
                        } else {
                            tmp.add(ch.code - 97)
                        }
                    } else {
                        if (isAlphaUpper(ch)) {
                            tmp.add(27) // as (shift)
                            tmp.add(ch.code - 65)
                        } else if (isMixed(ch)) {
                            submode = SUBMODE_MIXED
                            tmp.add(28) // ml
                            continue
                        } else {
                            tmp.add(29) // ps
                            tmp.add(PUNCTUATION[ch.code].toInt())
                        }
                    }
                }
                SUBMODE_MIXED -> {
                    if (isMixed(ch)) {
                        tmp.add(MIXED[ch.code].toInt())
                    } else {
                        if (isAlphaUpper(ch)) {
                            submode = SUBMODE_ALPHA
                            tmp.add(28) // al
                            continue
                        } else if (isAlphaLower(ch)) {
                            submode = SUBMODE_LOWER
                            tmp.add(27) // ll
                            continue
                        } else {
                            if (startpos + idx + 1 < count && isPunctuation(msg[startpos + idx + 1])) {
                                submode = SUBMODE_PUNCTUATION
                                tmp.add(25) // pl
                                continue
                            }
                            tmp.add(29) // ps
                            tmp.add(PUNCTUATION[ch.code].toInt())
                        }
                    }
                }
                else -> { // SUBMODE_PUNCTUATION
                    if (isPunctuation(ch)) {
                        tmp.add(PUNCTUATION[ch.code].toInt())
                    } else {
                        submode = SUBMODE_ALPHA
                        tmp.add(29) // al
                        continue
                    }
                }
            }
            idx++
        }

        var h = 0
        val len = tmp.size
        for (i in 0 until len) {
            val odd = (i % 2) != 0
            if (odd) {
                h = (h * 30) + tmp[i]
                target.add(h)
            } else {
                h = tmp[i]
            }
        }
        if ((len % 2) != 0) {
            target.add((h * 30) + 29)
        }
        return submode
    }

    private fun encodeBinary(
        bytes: ByteArray,
        startpos: Int,
        count: Int,
        startmode: Int,
        target: MutableList<Int>
    ) {
        if (count == 1 && startmode == TEXT_COMPACTION) {
            target.add(SHIFT_TO_BYTE)
        } else {
            if ((count % 6) == 0) {
                target.add(LATCH_TO_BYTE)
            } else {
                target.add(LATCH_TO_BYTE_PADDED)
            }
        }

        var idx = startpos
        if (count >= 6) {
            val chars = IntArray(5)
            while ((startpos + count - idx) >= 6) {
                var t = 0L
                for (i in 0 until 6) {
                    t = (t shl 8) or (bytes[idx + i].toLong() and 0xFF)
                }
                for (i in 0 until 5) {
                    chars[i] = (t % 900).toInt()
                    t /= 900
                }
                for (i in chars.indices.reversed()) {
                    target.add(chars[i])
                }
                idx += 6
            }
        }

        for (i in idx until startpos + count) {
            target.add(bytes[i].toInt() and 0xFF)
        }
    }

    private fun encodeNumeric(
        msg: String,
        startpos: Int,
        count: Int,
        target: MutableList<Int>
    ) {
        var idx = 0
        while (idx < count) {
            val len = min(44, count - idx)
            val part = "1" + msg.substring(startpos + idx, startpos + idx + len)

            val d = IntArray(part.length) { part[it] - '0' }
            var start = 0
            val tmp = mutableListOf<Int>()
            while (start < d.size) {
                var rem = 0
                for (i in start until d.size) {
                    val cur = rem * 10 + d[i]
                    d[i] = cur / 900
                    rem = cur % 900
                }
                tmp.add(rem)
                while (start < d.size && d[start] == 0) start++
            }

            for (i in tmp.indices.reversed()) {
                target.add(tmp[i])
            }
            idx += len
        }
    }

    private fun encodingECI(eci: Int, target: MutableList<Int>) {
        when {
            eci in 0..899 -> {
                target.add(ECI_CHARSET)
                target.add(eci)
            }
            eci < 810900 -> {
                target.add(ECI_GENERAL_PURPOSE)
                target.add(eci / 900 - 1)
                target.add(eci % 900)
            }
            eci < 811800 -> {
                target.add(ECI_USER_DEFINED)
                target.add(810900 - eci)
            }
            else -> throw IllegalArgumentException("ECI number not in valid range 0..811799, was $eci")
        }
    }

    private fun isDigit(ch: Char): Boolean = ch in '0'..'9'

    private fun isAlphaUpper(ch: Char): Boolean = ch == ' ' || ch in 'A'..'Z'

    private fun isAlphaLower(ch: Char): Boolean = ch == ' ' || ch in 'a'..'z'

    private fun isMixed(ch: Char): Boolean = ch.code in 0..127 && MIXED[ch.code] != (-1).toByte()

    private fun isPunctuation(ch: Char): Boolean = ch.code in 0..127 && PUNCTUATION[ch.code] != (-1).toByte()

    private fun isText(ch: Char): Boolean = ch == '\t' || ch == '\n' || ch == '\r' || (ch.code in 32..126)

    private fun determineConsecutiveDigitCount(msg: String, startpos: Int): Int {
        var count = 0
        var idx = startpos
        while (idx < msg.length && isDigit(msg[idx])) {
            count++
            idx++
        }
        return count
    }

    private fun determineConsecutiveTextCount(msg: String, startpos: Int): Int {
        val len = msg.length
        var idx = startpos
        while (idx < len) {
            var numericCount = 0
            while (numericCount < 13 && idx < len && isDigit(msg[idx])) {
                numericCount++
                idx++
            }
            if (numericCount >= 13) {
                return idx - startpos - numericCount
            }
            if (numericCount > 0) {
                continue
            }
            if (!isText(msg[idx])) {
                break
            }
            idx++
        }
        return idx - startpos
    }

    private fun determineConsecutiveBinaryCount(msg: String, startpos: Int): Int {
        val len = msg.length
        var idx = startpos
        while (idx < len) {
            var numericCount = 0
            var i = idx
            while (numericCount < 13 && i < len && isDigit(msg[i])) {
                numericCount++
                i++
            }
            if (numericCount >= 13) {
                return idx - startpos
            }
            idx++
        }
        return idx - startpos
    }
}
