package io.github.alexzhirkevich.qrose.options

import androidx.compose.runtime.Stable

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
    object : QrPixelShape, QrShapeModifier by SquareShape(size){}

@Stable
public fun QrPixelShape.Companion.circle(size: Float = 1f) : QrPixelShape =
    object : QrPixelShape, QrShapeModifier by CircleShape(size){}

@Stable
public fun QrPixelShape.Companion.roundCorners(radius : Float = .5f) : QrPixelShape =
    object : QrPixelShape, QrShapeModifier by RoundCornersShape(radius,true){}

@Stable
public fun QrPixelShape.Companion.verticalLines(width : Float = 1f) : QrPixelShape =
    object : QrPixelShape, QrShapeModifier by VerticalLinesShape(width){}

@Stable
public fun QrPixelShape.Companion.horizontalLines(width : Float = 1f) : QrPixelShape =
    object : QrPixelShape, QrShapeModifier by HorizontalLinesShape(width){}