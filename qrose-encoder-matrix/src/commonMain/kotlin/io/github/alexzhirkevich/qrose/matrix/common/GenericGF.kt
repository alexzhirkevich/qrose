package io.github.alexzhirkevich.qrose.matrix.common

internal class GenericGF(
    val primitive: Int,
    val size: Int,
    val generatorBase: Int
) {
    private val expTable = IntArray(size)
    private val logTable = IntArray(size)
    val zero: GenericGFPoly
    val one: GenericGFPoly

    init {
        var x = 1
        for (i in 0 until size) {
            expTable[i] = x
            x = x shl 1
            if (x >= size) {
                x = (x xor primitive) and (size - 1)
            }
        }
        for (i in 0 until size - 1) {
            logTable[expTable[i]] = i
        }
        zero = GenericGFPoly(this, intArrayOf(0))
        one = GenericGFPoly(this, intArrayOf(1))
    }

    fun buildMonomial(degree: Int, coefficient: Int): GenericGFPoly {
        require(degree >= 0)
        if (coefficient == 0) return zero
        val coefficients = IntArray(degree + 1)
        coefficients[0] = coefficient
        return GenericGFPoly(this, coefficients)
    }

    fun addOrSubtract(a: Int, b: Int): Int = a xor b

    fun exp(a: Int): Int = expTable[a]

    fun log(a: Int): Int {
        require(a != 0) { "log(0) is undefined" }
        return logTable[a]
    }

    fun inverse(a: Int): Int {
        require(a != 0) { "0 has no inverse" }
        return expTable[size - logTable[a] - 1]
    }

    fun multiply(a: Int, b: Int): Int {
        if (a == 0 || b == 0) return 0
        return expTable[(logTable[a] + logTable[b]) % (size - 1)]
    }

    companion object {
        val AZTEC_DATA_12 = GenericGF(0x1069, 4096, 1)
        val AZTEC_DATA_10 = GenericGF(0x409, 1024, 1)
        val AZTEC_DATA_8 = GenericGF(0x12D, 256, 1)
        val AZTEC_DATA_6 = GenericGF(0x43, 64, 1)
        val AZTEC_PARAM = GenericGF(0x13, 16, 1)
        val DATA_MATRIX_FIELD_256 = GenericGF(0x12D, 256, 1)
    }
}
