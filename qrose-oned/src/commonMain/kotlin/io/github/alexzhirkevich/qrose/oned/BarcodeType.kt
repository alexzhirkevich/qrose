package io.github.alexzhirkevich.qrose.oned

import io.github.alexzhirkevich.qrose.QroseEncoders

public enum class BarcodeType(public val encoder: BarcodeEncoder) {

    Codabar(QroseEncoders.Codabar()),
    Code39(QroseEncoders.Code39()),
    Code93(QroseEncoders.Code93()),
    Code128(QroseEncoders.Code128()),
    EAN8(QroseEncoders.EAN8()),
    EAN13(QroseEncoders.EAN13()),
    ITF(QroseEncoders.ITF()),
    UPCA(QroseEncoders.UPCA()),
    UPCE(QroseEncoders.UPCE())
}