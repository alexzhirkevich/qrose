package io.github.alexzhirkevich.qrose.matrix

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.Path
import io.github.alexzhirkevich.qrose.CircleShape
import io.github.alexzhirkevich.qrose.HorizontalLinesShape
import io.github.alexzhirkevich.qrose.RectangleShape
import io.github.alexzhirkevich.qrose.RoundCornersShape
import io.github.alexzhirkevich.qrose.ShapeModifier
import io.github.alexzhirkevich.qrose.SquareShape
import io.github.alexzhirkevich.qrose.VerticalLinesShape

@Immutable
public interface MatrixPixelShape : ShapeModifier {

    companion object {
        public val Default : MatrixPixelShape = square()
    }
}

@Stable
public fun MatrixPixelShape.Companion.square(size: Float = 1f) : MatrixPixelShape =
    object : MatrixPixelShape, ShapeModifier by SquareShape(size) {}

@Stable
public fun MatrixPixelShape.Companion.circle(size: Float = 1f) : MatrixPixelShape =
    object : MatrixPixelShape, ShapeModifier by CircleShape(size) {}

@Stable
public fun MatrixPixelShape.Companion.roundCorners(radius : Float = .5f) : MatrixPixelShape =
    object : MatrixPixelShape, ShapeModifier by RoundCornersShape(radius, true) {}

@Stable
public fun MatrixPixelShape.Companion.verticalLines(width : Float = 1f) : MatrixPixelShape =
    object : MatrixPixelShape, ShapeModifier by VerticalLinesShape(width) {}

@Stable
public fun MatrixPixelShape.Companion.horizontalLines(width : Float = 1f) : MatrixPixelShape =
    object : MatrixPixelShape, ShapeModifier by HorizontalLinesShape(width) {}