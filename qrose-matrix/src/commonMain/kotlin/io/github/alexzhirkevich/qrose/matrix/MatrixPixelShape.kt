package io.github.alexzhirkevich.qrose.matrix

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path

@Immutable
public interface MatrixPixelShape {

    public fun Path.addPixel(x: Float, y: Float, width: Float, height: Float): Path

    public object Default : MatrixPixelShape {
        override fun Path.addPixel(x: Float, y: Float, width: Float, height: Float): Path = apply {
            addRect(Rect(x, y, x + width, y + height))
        }
    }

    @Immutable
    public class RoundCorners(
        public val cornerRatio: Float = 0.35f
    ) : MatrixPixelShape {
        override fun Path.addPixel(x: Float, y: Float, width: Float, height: Float): Path = apply {
            val rx = width * cornerRatio.coerceIn(0f, 0.5f)
            val ry = height * cornerRatio.coerceIn(0f, 0.5f)
            addRoundRect(
                RoundRect(
                    left = x,
                    top = y,
                    right = x + width,
                    bottom = y + height,
                    radiusX = rx,
                    radiusY = ry
                )
            )
        }

        override fun equals(other: Any?): Boolean {
            if (this === other) return true
            if (other !is RoundCorners) return false
            return cornerRatio == other.cornerRatio
        }

        override fun hashCode(): Int = cornerRatio.hashCode()
    }

    public object Circle : MatrixPixelShape {
        override fun Path.addPixel(x: Float, y: Float, width: Float, height: Float): Path = apply {
            addOval(Rect(x, y, x + width, y + height))
        }
    }
}
