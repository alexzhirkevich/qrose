package io.github.alexzhirkevich.qrose.options

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Path

/**
 * Style of the qr-code eye internal ball.
 * */
@Stable
public interface QrBallShape : QrShapeModifier {

    public companion object {
        public val Default : QrBallShape = square()
    }
}

@Stable
public fun QrBallShape.Companion.square(size : Float = 1f) : QrBallShape =
    object : QrBallShape, QrShapeModifier by SquareShape(size){}

@Stable
public fun QrBallShape.Companion.circle(size : Float = 1f) : QrBallShape =
    object : QrBallShape, QrShapeModifier by CircleShape(size){}

@Stable
public fun QrBallShape.Companion.roundCorners(
    radius: Float,
    topLeft: Boolean = true,
    bottomLeft: Boolean = true,
    topRight: Boolean = true,
    bottomRight: Boolean = true,
) : QrBallShape = object : QrBallShape, QrShapeModifier by RoundCornersShape(
    cornerRadius = radius,
    topLeft = topLeft,
    bottomLeft = bottomLeft,
    topRight = topRight,
    bottomRight = bottomRight,
    withNeighbors = false
){}

@Stable
public fun QrBallShape.Companion.asPixel(pixelShape: QrPixelShape) : QrBallShape =
    AsPixelBallShape(pixelShape)



@Stable
private class AsPixelBallShape(
    private val pixelShape: QrPixelShape
) : QrBallShape {

    override fun Path.path(size: Float, neighbors: Neighbors): Path = apply {

        val matrix = QrCodeMatrix(3, QrCodeMatrix.PixelType.DarkPixel)

        repeat(3){ i ->
            repeat(3){ j ->
                addPath(
                    pixelShape.newPath(
                        size / 3,
                        matrix.neighbors(i,j)
                    ),
                    Offset(size/3 * i, size/3 * j)
                )
            }
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as AsPixelBallShape

        return pixelShape == other.pixelShape
    }

    override fun hashCode(): Int {
        return 31 * pixelShape.hashCode()
    }
}