package io.github.alexzhirkevich.qrose.options

import androidx.compose.runtime.Stable
import io.github.alexzhirkevich.qrose.CircleShape
import io.github.alexzhirkevich.qrose.HorizontalLinesShape
import io.github.alexzhirkevich.qrose.RoundCornersShape
import io.github.alexzhirkevich.qrose.ShapeModifier
import io.github.alexzhirkevich.qrose.SquareShape
import io.github.alexzhirkevich.qrose.VerticalLinesShape

/**
 * Style of the qr-code pixels.
 * */
@Stable
public fun interface QrPixelShape : QrShapeModifier {

    public companion object {
        public val Default : QrPixelShape = square()
    }
}

@Stable
public fun QrPixelShape.Companion.square(size: Float = 1f) : QrPixelShape =
    object : QrPixelShape, QrShapeModifier, ShapeModifier by SquareShape(size) {}

@Stable
public fun QrPixelShape.Companion.circle(size: Float = 1f) : QrPixelShape =
    object : QrPixelShape, QrShapeModifier, ShapeModifier by CircleShape(size) {}

@Stable
public fun QrPixelShape.Companion.roundCorners(radius : Float = .5f) : QrPixelShape =
    object : QrPixelShape, QrShapeModifier, ShapeModifier by RoundCornersShape(radius, true) {}

@Stable
public fun QrPixelShape.Companion.verticalLines(width : Float = 1f) : QrPixelShape =
    object : QrPixelShape, QrShapeModifier, ShapeModifier by VerticalLinesShape(width) {}

@Stable
public fun QrPixelShape.Companion.horizontalLines(width : Float = 1f) : QrPixelShape =
    object : QrPixelShape, QrShapeModifier, ShapeModifier by HorizontalLinesShape(width) {}