package io.github.alexzhirkevich.qrose.matrix.pdf417

/**
 * Bounds on the number of data columns and rows for a PDF417 symbol.
 *
 * @param minCols minimum number of data columns (excluding start/stop and indicator columns). Range: 1..30.
 * @param maxCols maximum number of data columns. Range: 1..30.
 * @param minRows minimum number of rows. Range: 3..90.
 * @param maxRows maximum number of rows. Range: 3..90.
 */
data class Pdf417Dimensions(
    val minCols: Int = 2,
    val maxCols: Int = 30,
    val minRows: Int = 3,
    val maxRows: Int = 90,
)
