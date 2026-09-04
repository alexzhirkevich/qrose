package io.github.alexzhirkevich.qrose.matrix.datamatrix

/**
 * Desired shape format for Data Matrix symbols.
 */
public enum class DataMatrixShape {
    /**
     * Choose the smallest symbol, preferring square over rectangular if both fit.
     */
    Auto,

    /**
     * Force square symbol shape.
     */
    Square,

    /**
     * Force rectangular symbol shape.
     */
    Rectangle
}
