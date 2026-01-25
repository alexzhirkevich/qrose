package io.github.alexzhirkevich.qrose.options

import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path

@Stable
internal class RectangleShape(
    val size: Float = 1f,
    val aspectRatio : Float = 1f,
    val cornerRadius : Float = 0f,
) : QrShapeModifier {

    override fun Path.path(size: Float, neighbors: Neighbors): Path = apply {
        val s = size * this@RectangleShape.size.coerceIn(0f, 1f)

        val sizeActual = if (aspectRatio > 1f){
            Size(s,s / aspectRatio)
        } else {
            Size(s * aspectRatio,s)
        }

        val offset = if (aspectRatio > 1f) {
            Offset((size - sizeActual.width)/2, 0f)
        } else {
            Offset(0f,(size - sizeActual.height)/2)
        }

        val corner = (cornerRadius.coerceIn(0f, .5f) * size).let { CornerRadius(it, it) }

        if (cornerRadius == 0f) {
            addRect(Rect(offset, sizeActual))
        } else {
            addRoundRect(
                RoundRect(Rect(offset, sizeActual), corner)
            )
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as RectangleShape

        if (size != other.size) return false
        if (aspectRatio != other.aspectRatio) return false
        if (cornerRadius != other.cornerRadius) return false

        return true
    }

    override fun hashCode(): Int {
        var result = size.hashCode()
        result = 31 * result + aspectRatio.hashCode()
        result = 31 * result + cornerRadius.hashCode()
        return result
    }

}

@Stable
internal class OvalShape(
    val size: Float = 1f,
    val aspectRatio : Float = 1f,
) : QrShapeModifier {

    override fun Path.path(size: Float, neighbors: Neighbors): Path = apply {
        val s = size * this@OvalShape.size.coerceIn(0f, 1f)

        val sizeActual = if (aspectRatio > 1f){
            Size(s,s / aspectRatio)
        } else {
            Size(s * aspectRatio,s)
        }

        val offset = if (aspectRatio > 1f) {
            Offset((size - sizeActual.width)/2, 0f)
        } else {
            Offset(0f,(size - sizeActual.height)/2)
        }

        addOval(Rect(offset, sizeActual))
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as RectangleShape

        if (size != other.size) return false
        if (aspectRatio != other.aspectRatio) return false

        return true
    }

    override fun hashCode(): Int {
        var result = size.hashCode()
        result = 31 * result + aspectRatio.hashCode()
        return result
    }

}

@Stable
internal class SquareShape(
    val size: Float = 1f
) : QrShapeModifier {

    override fun Path.path(size: Float, neighbors: Neighbors): Path = apply {
        val s = size * this@SquareShape.size.coerceIn(0f, 1f)
        val offset = (size - s) / 2

        addRect(
            Rect(
                Offset(offset, offset),
                Size(s, s)
            )
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as SquareShape

        return size == other.size
    }

    override fun hashCode(): Int {
        return size.hashCode()
    }
}

@Stable
internal class CircleShape(
   val size: Float
) : QrShapeModifier {

    override fun Path.path(size: Float, neighbors: Neighbors): Path = apply {
        val s = size * this@CircleShape.size.coerceIn(0f, 1f)
        val offset = (size - s) / 2
        
        addOval(
            Rect(
                Offset(offset, offset),
                Size(s, s)
            )
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as CircleShape

        return size == other.size
    }

    override fun hashCode(): Int {
        return size.hashCode()
    }
}

@Stable
internal class RoundCornersShape(
    val cornerRadius : Float,
    val withNeighbors : Boolean,
    val topLeft: Boolean = true,
    val bottomLeft: Boolean = true,
    val topRight: Boolean = true,
    val bottomRight: Boolean = true,
)  : QrShapeModifier {



    override fun Path.path(size: Float, neighbors: Neighbors): Path = apply {

        val corner = (cornerRadius.coerceIn(0f, .5f) * size).let { CornerRadius(it, it) }

        addRoundRect(
            RoundRect(
                Rect(0f, 0f, size, size),
                topLeft = if (topLeft && (withNeighbors.not() || neighbors.top.not() && neighbors.left.not()))
                    corner else CornerRadius.Zero,
                topRight = if (topRight && (withNeighbors.not() || neighbors.top.not() && neighbors.right.not()))
                    corner else CornerRadius.Zero,
                bottomRight = if (bottomRight && (withNeighbors.not() || neighbors.bottom.not() && neighbors.right.not()))
                    corner else CornerRadius.Zero,
                bottomLeft = if (bottomLeft && (withNeighbors.not() || neighbors.bottom.not() && neighbors.left.not()))
                    corner else CornerRadius.Zero
            )
        )
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as RoundCornersShape

        if (cornerRadius != other.cornerRadius) return false
        if (withNeighbors != other.withNeighbors) return false
        if (topLeft != other.topLeft) return false
        if (bottomLeft != other.bottomLeft) return false
        if (topRight != other.topRight) return false
        if (bottomRight != other.bottomRight) return false

        return true
    }

    override fun hashCode(): Int {
        var result = cornerRadius.hashCode()
        result = 31 * result + withNeighbors.hashCode()
        result = 31 * result + topLeft.hashCode()
        result = 31 * result + bottomLeft.hashCode()
        result = 31 * result + topRight.hashCode()
        result = 31 * result + bottomRight.hashCode()
        return result
    }

}

@Stable
internal class VerticalLinesShape(
    private val width : Float
) : QrShapeModifier {

    override fun Path.path(size: Float, neighbors: Neighbors): Path = apply {

        val padding = (size * (1 - width.coerceIn(0f, 1f)))

        if (neighbors.top) {
            addRect(Rect(Offset(padding, 0f), Size(size - padding * 2, size / 2)))
        } else {
            addArc(Rect(Offset(padding, 0f), Size(size - padding * 2, size)), 180f, 180f)
        }

        if (neighbors.bottom) {
            addRect(Rect(Offset(padding, size / 2), Size(size - padding * 2, size / 2)))
        } else {
            addArc(Rect(Offset(padding, 0f), Size(size - padding * 2, size)), 0f, 180f)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as VerticalLinesShape

        return width == other.width
    }

    override fun hashCode(): Int {
        return width.hashCode()
    }
}

@Stable
internal class HorizontalLinesShape(
    private val width : Float
) : QrShapeModifier {

    override fun Path.path(size: Float, neighbors: Neighbors): Path = apply {

        val padding = (size * (1 - width.coerceIn(0f, 1f)))

        if (neighbors.left) {
            addRect(Rect(Offset(0f, padding), Size(size / 2, size - padding * 2)))
        } else {
            addArc(Rect(Offset(0f, padding), Size(size, size - padding * 2)), 90f, 180f)

        }

        if (neighbors.right) {
            addRect(Rect(Offset(size / 2, padding), Size(size / 2, size - padding * 2)))
        } else {
            addArc(Rect(Offset(0f, padding), Size(size, size - padding * 2)), -90f, 180f)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as HorizontalLinesShape

        return width == other.width
    }

    override fun hashCode(): Int {
        return width.hashCode()
    }
}
