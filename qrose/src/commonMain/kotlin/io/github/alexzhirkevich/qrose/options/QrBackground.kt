package io.github.alexzhirkevich.qrose.options

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter

@Stable
public class QrBackground(
    val painter: Painter? = null,
    val fill: Brush? = null,
    val shape: Shape = RectangleShape,
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as QrBackground

        if (painter != other.painter) return false
        if (fill != other.fill) return false
        if (shape != other.shape) return false

        return true
    }

    override fun hashCode(): Int {
        var result = painter?.hashCode() ?: 0
        result = 31 * result + (fill?.hashCode() ?: 0)
        result = 31 * result + shape.hashCode()
        return result
    }

    public fun copy(
        painter: Painter? = this.painter,
        brush: Brush? = this.fill,
        shape: Shape = this.shape,
    ) = QrBackground(
        painter = painter,
        fill = brush,
        shape = shape,
    )
}

