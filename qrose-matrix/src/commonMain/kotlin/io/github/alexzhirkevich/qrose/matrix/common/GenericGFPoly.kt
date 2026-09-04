package io.github.alexzhirkevich.qrose.matrix.common

internal class GenericGFPoly(
    private val field: GenericGF,
    coefficients: IntArray
) {
    val coefficients: IntArray

    init {
        require(coefficients.isNotEmpty())
        if (coefficients.size > 1 && coefficients[0] == 0) {
            var firstNonZero = 1
            while (firstNonZero < coefficients.size && coefficients[firstNonZero] == 0) {
                firstNonZero++
            }
            if (firstNonZero == coefficients.size) {
                this.coefficients = field.zero.coefficients
            } else {
                val newCoefficients = IntArray(coefficients.size - firstNonZero)
                coefficients.copyInto(newCoefficients, 0, firstNonZero, coefficients.size)
                this.coefficients = newCoefficients
            }
        } else {
            this.coefficients = coefficients
        }
    }

    val degree: Int
        get() = coefficients.size - 1

    val isZero: Boolean
        get() = coefficients[0] == 0

    fun getCoefficient(degree: Int): Int = coefficients[coefficients.size - 1 - degree]

    fun addOrSubtract(other: GenericGFPoly): GenericGFPoly {
        if (isZero) return other
        if (other.isZero) return this

        var smallerCoefficients = this.coefficients
        var largerCoefficients = other.coefficients
        if (smallerCoefficients.size > largerCoefficients.size) {
            val temp = smallerCoefficients
            smallerCoefficients = largerCoefficients
            largerCoefficients = temp
        }

        val sumDiff = IntArray(largerCoefficients.size)
        val lengthDiff = largerCoefficients.size - smallerCoefficients.size
        largerCoefficients.copyInto(sumDiff, 0, 0, lengthDiff)

        for (i in lengthDiff until largerCoefficients.size) {
            sumDiff[i] = field.addOrSubtract(smallerCoefficients[i - lengthDiff], largerCoefficients[i])
        }

        return GenericGFPoly(field, sumDiff)
    }

    fun multiply(other: GenericGFPoly): GenericGFPoly {
        if (isZero || other.isZero) return field.zero

        val aCoeff = this.coefficients
        val aLen = aCoeff.size
        val bCoeff = other.coefficients
        val bLen = bCoeff.size
        val product = IntArray(aLen + bLen - 1)

        for (i in 0 until aLen) {
            val aCoeffI = aCoeff[i]
            for (j in 0 until bLen) {
                product[i + j] = field.addOrSubtract(product[i + j], field.multiply(aCoeffI, bCoeff[j]))
            }
        }
        return GenericGFPoly(field, product)
    }

    fun multiply(scalar: Int): GenericGFPoly {
        if (scalar == 0) return field.zero
        if (scalar == 1) return this

        val size = coefficients.size
        val product = IntArray(size)
        for (i in 0 until size) {
            product[i] = field.multiply(coefficients[i], scalar)
        }
        return GenericGFPoly(field, product)
    }

    fun multiplyByMonomial(degree: Int, coefficient: Int): GenericGFPoly {
        require(degree >= 0)
        if (coefficient == 0) return field.zero

        val size = coefficients.size
        val product = IntArray(size + degree)
        for (i in 0 until size) {
            product[i] = field.multiply(coefficients[i], coefficient)
        }
        return GenericGFPoly(field, product)
    }

    fun divide(other: GenericGFPoly): Array<GenericGFPoly> {
        require(!other.isZero) { "Divide by 0" }

        var quotient = field.zero
        var remainder = this

        val denominatorLeadingTerm = other.getCoefficient(other.degree)
        val inverseDenominatorLeadingTerm = field.inverse(denominatorLeadingTerm)

        while (remainder.degree >= other.degree && !remainder.isZero) {
            val degreeDifference = remainder.degree - other.degree
            val scale = field.multiply(remainder.getCoefficient(remainder.degree), inverseDenominatorLeadingTerm)
            val term = other.multiplyByMonomial(degreeDifference, scale)
            val iterationQuotient = field.buildMonomial(degreeDifference, scale)
            quotient = quotient.addOrSubtract(iterationQuotient)
            remainder = remainder.addOrSubtract(term)
        }

        return arrayOf(quotient, remainder)
    }
}
