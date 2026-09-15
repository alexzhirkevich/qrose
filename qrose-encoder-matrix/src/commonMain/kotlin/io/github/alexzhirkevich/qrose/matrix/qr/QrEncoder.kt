package io.github.alexzhirkevich.qrose.matrix.qr

import io.github.alexzhirkevich.qrose.matrix.Matrix2D
import io.github.alexzhirkevich.qrose.matrix.MatrixCodeEncoder
import io.github.alexzhirkevich.qrose.matrix.qr.internals.QRCode

class QrEncoder(
    private val errorCorrectionLevel: QrErrorCorrection = QrErrorCorrection.L,
    private val maskPattern: QrMaskPattern = QrMaskPattern.PATTERN000,
) : MatrixCodeEncoder {

    override fun encode(data: String): Matrix2D {
        return QRCode(data, errorCorrectionLevel).encode(maskPattern = maskPattern)
    }
}