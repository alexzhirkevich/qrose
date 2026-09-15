package io.github.alexzhirkevich.qrose.matrix.pdf417

/**
 * Bounds on the number of data columns and rows for a PDF417 symbol.
 *
 * @param minCols minimum number of data columns (excluding start/stop and indicator columns). Range: 1..30.
 * @param maxCols maximum number of data columns. Range: 1..30.
 * @param minRows minimum number of rows. Range: 3..90.
 * @param maxRows maximum number of rows. Range: 3..90.
 */
class Pdf417Dimensions(
    val minCols: Int = 2,
    val maxCols: Int = 30,
    val minRows: Int = 3,
    val maxRows: Int = 90,
){
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Pdf417Dimensions

        if (minCols != other.minCols) return false
        if (maxCols != other.maxCols) return false
        if (minRows != other.minRows) return false
        if (maxRows != other.maxRows) return false

        return true
    }

    override fun hashCode(): Int {
        var result = minCols
        result = 31 * result + maxCols
        result = 31 * result + minRows
        result = 31 * result + maxRows
        return result
    }

    override fun toString(): String {
        return "Pdf417Dimensions(minCols=$minCols, maxCols=$maxCols, minRows=$minRows, maxRows=$maxRows)"
    }

    companion object {
        val Default = Pdf417Dimensions()
    }
}
