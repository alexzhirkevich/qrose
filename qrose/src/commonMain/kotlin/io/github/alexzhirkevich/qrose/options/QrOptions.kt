package io.github.alexzhirkevich.qrose.options

import androidx.compose.runtime.Stable
import io.github.alexzhirkevich.qrose.options.dsl.InternalQrOptionsBuilderScope
import io.github.alexzhirkevich.qrose.options.dsl.QrOptionsBuilderScope

@Stable
public fun QrOptions(block : QrOptionsBuilderScope.() -> Unit) : QrOptions {
    val builder = QrOptions.Builder()
    InternalQrOptionsBuilderScope(builder).apply(block)
    return builder.build()
}

/**
 * Styling options of the QR code
 *
 * @param shapes shapes of the QR code pattern and its parts
 * @param colors colors of the QR code parts
 * @param logo middle image
 * @param errorCorrectionLevel level of error correction
 * @param fourEyed enable fourth eye
 * */
@Stable
public class QrOptions(
    public val shapes: QrShapes = QrShapes(),
    public val colors : QrColors = QrColors(),
    public val logo : QrLogo = QrLogo(),
    public val errorCorrectionLevel: QrErrorCorrectionLevel = QrErrorCorrectionLevel.Auto,
    public val fourEyed : Boolean = false,
) {

    public fun copy(
        shapes: QrShapes = this.shapes,
        colors : QrColors = this.colors,
        logo : QrLogo = this.logo,
        errorCorrectionLevel: QrErrorCorrectionLevel = this.errorCorrectionLevel,
        fourthEyeEnabled : Boolean = this.fourEyed,
    ): QrOptions = QrOptions(
        shapes = shapes,
        colors = colors,
        logo = logo,
        errorCorrectionLevel = errorCorrectionLevel,
        fourEyed = fourthEyeEnabled
    )
//
//    override fun equals(other: Any?): Boolean {
//        if (this === other) return true
//        if (other == null || this::class != other::class) return false
//
//        other as QrOptions
//
//        if (shapes != other.shapes) return false
//        if (colors != other.colors) return false
//        if (logo != other.logo) return false
//        if (errorCorrectionLevel != other.errorCorrectionLevel) return false
//        if (fourEyed != other.fourEyed) return false
//
//        return true
//    }

//    override fun hashCode(): Int {
//        var result = shapes.hashCode()
//        result = 31 * result + colors.hashCode()
//        result = 31 * result + logo.hashCode()
//        result = 31 * result + errorCorrectionLevel.hashCode()
//        result = 31 * result + fourEyed.hashCode()
//        return result
//    }


    internal class Builder {

        var shapes: QrShapes = QrShapes()
        var colors: QrColors = QrColors()
        var logo: QrLogo = QrLogo()
        var errorCorrectionLevel: QrErrorCorrectionLevel = QrErrorCorrectionLevel.Auto
        var fourthEyeEnabled: Boolean = false

        fun build(): QrOptions = QrOptions(
            shapes = shapes,
            colors = colors,
            logo = logo,
            errorCorrectionLevel = errorCorrectionLevel,
            fourEyed = fourthEyeEnabled
        )
    }
}

