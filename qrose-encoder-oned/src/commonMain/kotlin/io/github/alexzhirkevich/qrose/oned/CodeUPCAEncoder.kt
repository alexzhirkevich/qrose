package io.github.alexzhirkevich.qrose.oned

internal object CodeUPCAEncoder: BarcodeEncoder {
    override fun encode(data : String) = CodeEAN13Encoder.encode("0$data")
}