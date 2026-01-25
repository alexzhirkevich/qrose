package io.github.alexzhirkevich.qrose.options

import androidx.compose.runtime.Stable
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.addOutline

@Stable
public interface QrLogoShape : QrShapeModifier {

    public companion object {
        public val Default : QrLogoShape = object : QrLogoShape, QrShapeModifier by SquareShape(){}
    }

}

@Stable
public fun QrLogoShape.Companion.circle() : QrLogoShape=
    object  : QrLogoShape, QrShapeModifier by CircleShape(1f){}

@Stable
public fun QrLogoShape.Companion.rect(aspectRatio : Float, cornerRadius : Float = 0f) : QrLogoShape=
    object  : QrLogoShape, QrShapeModifier by RectangleShape(1f, aspectRatio,cornerRadius){}

@Stable
public fun QrLogoShape.Companion.oval(aspectRatio : Float) : QrLogoShape=
    object  : QrLogoShape, QrShapeModifier by OvalShape(1f, aspectRatio){}


@Stable
public fun QrLogoShape.Companion.roundCorners(radius: Float) : QrLogoShape=
    object  : QrLogoShape, QrShapeModifier by RoundCornersShape(radius, false){}
