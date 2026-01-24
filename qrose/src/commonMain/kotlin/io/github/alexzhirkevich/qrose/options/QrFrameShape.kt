package io.github.alexzhirkevich.qrose.options

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path

/**
 * Style of the qr-code eye frame.
 */
@Stable
public interface QrFrameShape : QrShapeModifier {

    public companion object {
        public val Default : QrFrameShape = square()
    }
}

@Stable
public fun QrFrameShape.Companion.square(size: Float = 1f) : QrFrameShape =
    SquareFrameShape(size)

@Stable
public fun QrFrameShape.Companion.circle(size: Float = 1f) : QrFrameShape =
    CircleFrameShape(size)

@Stable
public fun QrFrameShape.Companion.roundCorners(
    corner: Float,
    width: Float = 1f,
    topLeft: Boolean = true,
    bottomLeft: Boolean = true,
    topRight: Boolean = true,
    bottomRight: Boolean = true,
) : QrFrameShape = RoundCornersFrameShape(
    corner = corner,
    width = width,
    topLeft = topLeft,
    bottomLeft = bottomLeft,
    topRight = topRight,
    bottomRight = bottomRight
)

@Stable
public fun QrFrameShape.Companion.asPixel(pixelShape: QrPixelShape) : QrFrameShape =
    AsPixelFrameShape(pixelShape)


@Stable
private class AsPixelFrameShape(
    val pixelShape: QrPixelShape
) : QrFrameShape {

    override fun Path.path(size: Float, neighbors: Neighbors): Path = apply {

        val matrix = QrCodeMatrix(7)

        repeat(7) { i ->
            repeat(7) { j ->
                matrix[i, j] = if (i == 0 || j == 0 || i == 6 || j == 6)
                    QrCodeMatrix.PixelType.DarkPixel else QrCodeMatrix.PixelType.Background
            }
        }

        repeat(7) { i ->
            repeat(7) { j ->
                if (matrix[i, j] == QrCodeMatrix.PixelType.DarkPixel)
                    addPath(
                        pixelShape.newPath(
                            size / 7,
                            matrix.neighbors(i, j)
                        ),
                        Offset(size / 7 * i, size / 7 * j)
                    )
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as AsPixelFrameShape

        return pixelShape == other.pixelShape
    }

    override fun hashCode(): Int {
        return 31 * pixelShape.hashCode()
    }
}

@Immutable
private class SquareFrameShape(
    private val size: Float = 1f
) : QrFrameShape {
    override fun Path.path(size: Float, neighbors: Neighbors): Path = apply {
        val width = size / 7f * this@SquareFrameShape.size.coerceAtLeast(0f)

        addRect(
            Rect(0f, 0f, size, size)
        )
        addRect(
            Rect(width, width, size - width, size - width)
        )
    }
}


@Immutable
private class CircleFrameShape(
    private val size: Float = 1f
) : QrFrameShape {
    override fun Path.path(size: Float, neighbors: Neighbors): Path = apply {
        val width = size / 7f * this@CircleFrameShape.size.coerceAtLeast(0f)

        addOval(
            Rect(0f, 0f, size, size)
        )
        addOval(
            Rect(width, width, size - width, size - width)
        )
    }
}

@Immutable
private class RoundCornersFrameShape(
    private val corner: Float,
    private val width: Float = 1f,
    private val topLeft: Boolean = true,
    private val bottomLeft: Boolean = true,
    private val topRight: Boolean = true,
    private val bottomRight: Boolean = true,
) : QrFrameShape {
    override fun Path.path(size: Float, neighbors: Neighbors): Path = apply{

        val width = size / 7f * width.coerceAtLeast(0f)

        val realCorner = corner.coerceIn(0f, .5f)
        val outerCornerSize = realCorner * size
        val innerCornerSize = realCorner * (size - 4 * width)

        val outer = CornerRadius(outerCornerSize, outerCornerSize)
        val inner = CornerRadius(innerCornerSize, innerCornerSize)

        addRoundRect(
            RoundRect(
                Rect(0f, 0f, size, size),
                topLeft = if (topLeft) outer else CornerRadius.Zero,
                topRight = if (topRight) outer else CornerRadius.Zero,
                bottomLeft = if (bottomLeft) outer else CornerRadius.Zero,
                bottomRight = if (bottomRight) outer else CornerRadius.Zero,
            )
        )
        addRoundRect(
            RoundRect(
                Rect(width, width, size - width, size - width),
                topLeft = if (topLeft) inner else CornerRadius.Zero,
                topRight = if (topRight) inner else CornerRadius.Zero,
                bottomLeft = if (bottomLeft) inner else CornerRadius.Zero,
                bottomRight = if (bottomRight) inner else CornerRadius.Zero,
            )
        )
    }
}
