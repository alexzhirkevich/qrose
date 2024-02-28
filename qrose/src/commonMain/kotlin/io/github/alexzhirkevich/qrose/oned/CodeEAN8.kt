package io.github.alexzhirkevich.qrose.oned

internal class CodeEAN8  {
    private val CODE_WIDTH = 3 + 7 * 4 +  // left bars
            5 + 7 * 4 +  // right bars
            3 // end guard


    /**
     * @return a byte array of horizontal pixels (false = white, true = black)
     */
    fun encode(contents: String): BooleanArray {
        var actualContentns = contents
        val length = actualContentns.length
        when (length) {
            7 -> {
                // No check digit present, calculate it and add it
                val check: Int =  getStandardUPCEANChecksum(actualContentns)
                actualContentns += check
            }

            8 -> if (!checkStandardUPCEANChecksum(actualContentns)) {
                throw IllegalArgumentException("Contents do not pass checksum")
            }

            else -> throw IllegalArgumentException(
                "Requested contents should be 7 or 8 digits long, but got $length"
            )
        }
        actualContentns.requireNumeric()
        val result = BooleanArray(CODE_WIDTH)
        var pos = 0
        pos += appendPattern(result, pos, START_END_PATTERN, true)
        for (i in 0..3) {
            val digit = actualContentns[i].digitToIntOrNull() ?: -1
            pos += appendPattern(
                result,
                pos,
                L_PATTERNS.get(digit),
                false
            )
        }
        pos += appendPattern(
            result,
            pos,
            MIDDLE_PATTERN,
            false
        )
        for (i in 4..7) {
            val digit = actualContentns[i].digitToIntOrNull() ?: -1
            pos += appendPattern(result, pos, L_PATTERNS.get(digit), true)
        }
        appendPattern(result, pos, START_END_PATTERN, true)
        return result
    }

    companion object {
        fun checkStandardUPCEANChecksum(s: CharSequence): Boolean {
            val length = s.length
            if (length == 0) {
                return false
            }
            val check = s[length - 1].digitToIntOrNull() ?: -1
            return getStandardUPCEANChecksum(s.subSequence(0, length - 1)) == check
        }

        fun getStandardUPCEANChecksum(s: CharSequence): Int {
            val length = s.length
            var sum = 0
            run {
                var i = length - 1
                while (i >= 0) {
                    val digit = s[i].code - '0'.code
                    if (digit < 0 || digit > 9) {
                        throw IllegalStateException("Illegal contents")
                    }
                    sum += digit
                    i -= 2
                }
            }
            sum *= 3
            var i = length - 2
            while (i >= 0) {
                val digit = s[i].code - '0'.code
                if (digit < 0 || digit > 9) {
                    throw IllegalStateException("Illegal contents")
                }
                sum += digit
                i -= 2
            }
            return (1000 - sum) % 10
        }

        // These two values are critical for determining how permissive the decoding will be.
        // We've arrived at these values through a lot of trial and error. Setting them any higher
        // lets false positives creep in quickly.
        private const val MAX_AVG_VARIANCE = 0.48f
        private const val MAX_INDIVIDUAL_VARIANCE = 0.7f

        /**
         * Start/end guard pattern.
         */
        val START_END_PATTERN by lazy {
            intArrayOf(1, 1, 1)
        }

        /**
         * Pattern marking the middle of a UPC/EAN pattern, separating the two halves.
         */
        val MIDDLE_PATTERN by lazy {
            intArrayOf(1, 1, 1, 1, 1)
        }

        /**
         * end guard pattern.
         */
        val END_PATTERN by lazy {
            intArrayOf(1, 1, 1, 1, 1, 1)
        }
        /**
         * "Odd", or "L" patterns used to encode UPC/EAN digits.
         */
        val L_PATTERNS by lazy {
            arrayOf(
                intArrayOf(3, 2, 1, 1),
                intArrayOf(2, 2, 2, 1),
                intArrayOf(2, 1, 2, 2),
                intArrayOf(1, 4, 1, 1),
                intArrayOf(1, 1, 3, 2),
                intArrayOf(1, 2, 3, 1),
                intArrayOf(1, 1, 1, 4),
                intArrayOf(1, 3, 1, 2),
                intArrayOf(1, 2, 1, 3),
                intArrayOf(3, 1, 1, 2)
            )
        }

        val L_AND_G_PATTERNS by lazy {
            buildList(20) {
                addAll(L_PATTERNS)

                for (i in 10..19) {
                    val widths: IntArray = L_PATTERNS[i - 10]
                    val reversedWidths = IntArray(widths.size)
                    for (j in widths.indices) {
                        reversedWidths[j] = widths[widths.size - j - 1]
                    }
                    add(reversedWidths)
                }
            }
        }

        val FIRST_DIGIT_ENCODINGS by lazy {
            intArrayOf(
                0x00, 0x0B, 0x0D, 0xE, 0x13, 0x19, 0x1C, 0x15, 0x16, 0x1A
            )
        }
    }
}

internal fun String.requireNumeric() = require(all { it.isDigit() }){
    "Input should only contain digits 0-9"
}