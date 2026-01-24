package io.github.alexzhirkevich.qrose.oned

/**
 * Single dimension barcode encoder.
 * */
public interface BarcodeEncoder {

    /**
     * Encodes string [data] into barcode [BooleanArray] where 1 is a black bar and 0 is a white bar.
     *
     * Illegal contents can throw exceptions
     * */
    public fun encode(data : String) : BooleanArray
}