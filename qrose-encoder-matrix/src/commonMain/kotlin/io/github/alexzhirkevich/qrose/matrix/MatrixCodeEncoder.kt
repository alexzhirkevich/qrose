package io.github.alexzhirkevich.qrose.matrix

interface MatrixCodeEncoder {

    /**
     * Encode the string [data] to a barcode bit matrix.
     * Foreground modules (dots) are encoded as **true** and background modules (spaces) as **false**
     * */
    fun encode(data : String) : Matrix2D
}