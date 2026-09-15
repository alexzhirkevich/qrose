package io.github.alexzhirkevich.qrose.oned

interface BarcodeEncoder {

    /**
     * Encode the string [data] to a barcode bit array.
     * Bars are encoded as **true** and spaces as **false**
     * */
    fun encode(data : String) : BooleanArray
}