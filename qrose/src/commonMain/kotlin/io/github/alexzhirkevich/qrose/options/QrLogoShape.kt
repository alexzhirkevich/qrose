package io.github.alexzhirkevich.qrose.options

import androidx.compose.runtime.Stable
import io.github.alexzhirkevich.qrose.CircleShape
import io.github.alexzhirkevich.qrose.OvalShape
import io.github.alexzhirkevich.qrose.RectangleShape
import io.github.alexzhirkevich.qrose.RoundCornersShape
import io.github.alexzhirkevich.qrose.ShapeModifier
import io.github.alexzhirkevich.qrose.SquareShape

@Stable
public interface QrLogoShape : QrShapeModifier {

    public companion object {
        public val Default : QrLogoShape = rect(1f)
    }

}

@Stable
public fun QrLogoShape.Companion.circle() : QrLogoShape =
    object  : QrLogoShape, QrShapeModifier, ShapeModifier by CircleShape(1f) {}

@Stable
public fun QrLogoShape.Companion.rect(aspectRatio : Float, cornerRadius : Float = 0f) : QrLogoShape =
    object  : QrLogoShape, QrShapeModifier, ShapeModifier by RectangleShape(
        1f,
        aspectRatio,
        cornerRadius
    ) {}

@Stable
public fun QrLogoShape.Companion.oval(aspectRatio : Float) : QrLogoShape =
    object  : QrLogoShape, QrShapeModifier, ShapeModifier by OvalShape(1f, aspectRatio) {}


@Stable
public fun QrLogoShape.Companion.roundCorners(radius: Float) : QrLogoShape =
    object  : QrLogoShape, QrShapeModifier, ShapeModifier by RoundCornersShape(radius, false) {}
