package io.github.alexzhirkevich.qrose.options

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.painter.Painter

/**
 * Logo (middle image) of the QR code.
 *
 * @param painter middle image.
 * @param size image size in fraction relative to QR code size
 * @param padding style and size of the QR code padding.
 * Can be used without [painter] if you want to place a logo manually.
 * @param shape shape of the logo padding
 * */
@Stable
public class QrLogo(
    val painter: Painter? = null,
    val size: Float = 0.25f,
    val padding: QrLogoPadding = QrLogoPadding.Empty,
    val shape: QrLogoShape = QrLogoShape.Default,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as QrLogo

        if (painter != other.painter) return false
        if (size != other.size) return false
        if (padding != other.padding) return false
        if (shape != other.shape) return false

        return true
    }

    override fun hashCode(): Int {
        var result = painter?.hashCode() ?: 0
        result = 31 * result + size.hashCode()
        result = 31 * result + padding.hashCode()
        result = 31 * result + shape.hashCode()
        return result
    }

    fun copy(
        painter: Painter?= this.painter,
        size: Float = this.size,
        padding: QrLogoPadding = this.padding,
        shape: QrLogoShape = this.shape,
    ): QrLogo = QrLogo(
        painter = painter,
        size = size,
        padding = padding,
        shape = shape,
    )
}

