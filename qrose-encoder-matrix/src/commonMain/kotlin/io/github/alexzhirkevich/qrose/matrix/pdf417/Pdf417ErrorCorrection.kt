package io.github.alexzhirkevich.qrose.matrix.pdf417

/**
 * PDF417 Reed-Solomon error correction calculations according to ISO/IEC 15438.
 */
internal object Pdf417ErrorCorrection {

    fun getErrorCorrectionCodewordCount(level: Int): Int {
        require(level in 0..8) { "Error correction level must be between 0 and 8, but was $level" }
        return 1 shl (level + 1)
    }

    fun getRecommendedMinimumErrorCorrectionLevel(dataCodewordsCount: Int): Int {
        return when {
            dataCodewordsCount <= 0 -> 0
            dataCodewordsCount <= 40 -> 2
            dataCodewordsCount <= 160 -> 3
            dataCodewordsCount <= 320 -> 4
            dataCodewordsCount <= 863 -> 5
            else -> 6
        }
    }

    /**
     * Generates error correction codewords for the given data codewords.
     */
    fun generateErrorCorrection(dataCodewords: IntArray, errorCorrectionLevel: Int): IntArray {
        val k = getErrorCorrectionCodewordCount(errorCorrectionLevel)
        val e = IntArray(k)
        val coeffs = Pdf417Codewords.EC_COEFFICIENTS[errorCorrectionLevel]

        for (i in dataCodewords.indices) {
            val t1 = (dataCodewords[i] + e[k - 1]) % 929
            for (j in k - 1 downTo 1) {
                val t2 = (t1 * coeffs[j]) % 929
                val t3 = 929 - t2
                e[j] = (e[j - 1] + t3) % 929
            }
            val t2 = (t1 * coeffs[0]) % 929
            val t3 = 929 - t2
            e[0] = t3 % 929
        }

        val result = IntArray(k)
        for (j in k - 1 downTo 0) {
            val v = if (e[j] != 0) 929 - e[j] else 0
            result[k - 1 - j] = v
        }
        return result
    }
}
