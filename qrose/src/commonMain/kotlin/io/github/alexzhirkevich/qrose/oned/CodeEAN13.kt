package io.github.alexzhirkevich.qrose.oned


internal class CodeEAN13 {

    private val CODE_WIDTH = 3 + 7 * 6 +  // left bars
            5 + 7 * 6 +  // right bars
            3 // end guard


    fun encode(contents: String): BooleanArray {
        var contents = contents
        val length = contents.length
        when (length) {
            12 -> {
                contents +=  CodeEAN8.getStandardUPCEANChecksum(contents)
            }

            13 -> if (!CodeEAN8.checkStandardUPCEANChecksum(contents)) {
                throw IllegalArgumentException("Contents do not pass checksum")
            }

            else -> throw IllegalArgumentException(
                "Requested contents should be 12 or 13 digits long, but got $length"
            )
        }
        contents.requireNumeric()
        val firstDigit = contents[0].digitToIntOrNull() ?: -1
        val parities: Int = CodeEAN8.FIRST_DIGIT_ENCODINGS[firstDigit]
        val result = BooleanArray(CODE_WIDTH)
        var pos = 0
        pos += appendPattern(result, pos, CodeEAN8.START_END_PATTERN, true)

        // See EAN13Reader for a description of how the first digit & left bars are encoded
        for (i in 1..6) {
            var digit = contents[i].digitToIntOrNull() ?: -1
            if (parities shr 6 - i and 1 == 1) {
                digit += 10
            }
            pos += appendPattern(
                result,
                pos,
                CodeEAN8.L_AND_G_PATTERNS.get(digit),
                false
            )
        }
        pos += appendPattern(result, pos, CodeEAN8.MIDDLE_PATTERN, false)
        for (i in 7..12) {
            val digit = contents[i].digitToIntOrNull() ?: -1
            pos += appendPattern(result, pos, CodeEAN8.L_PATTERNS.get(digit), true)
        }
        appendPattern(result, pos, CodeEAN8.START_END_PATTERN, true)
        return result
    }

}