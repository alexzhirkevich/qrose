package io.github.alexzhirkevich.qrose.options

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable
import io.github.alexzhirkevich.qrose.options.dsl.InternalQrOptionsBuilderScope
import io.github.alexzhirkevich.qrose.options.dsl.QrOptionsBuilderScope
import io.github.alexzhirkevich.qrose.options.QrLogo
import io.github.alexzhirkevich.qrose.options.QrShapes
import io.github.alexzhirkevich.qrose.options.QrColors

fun QrOptions(block : QrOptionsBuilderScope.() -> Unit) : QrOptions {
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
@Immutable
class QrOptions(
    val shapes: QrShapes = QrShapes(),
    val colors : QrColors = QrColors(),
    val logo : QrLogo = QrLogo(),
    val errorCorrectionLevel: QrErrorCorrectionLevel = QrErrorCorrectionLevel.Auto,
    val fourEyed : Boolean = false,
) {

    fun copy(
        shapes: QrShapes = this.shapes,
        colors : QrColors = this.colors,
        logo : QrLogo = this.logo,
        errorCorrectionLevel: QrErrorCorrectionLevel = this.errorCorrectionLevel,
        fourthEyeEnabled : Boolean = this.fourEyed,
    ) = QrOptions(
        shapes = shapes,
        colors = colors,
        logo = logo,
        errorCorrectionLevel = errorCorrectionLevel,
        fourEyed = fourthEyeEnabled
    )


    override fun equals(other: Any?): Boolean {
        if (other !is QrOptions)
            return false

        return shapes == other.shapes &&
                colors == other.colors &&
                logo == other.logo &&
                errorCorrectionLevel == other.errorCorrectionLevel &&
                fourEyed == other.fourEyed
    }

    override fun hashCode(): Int {
        return (((((shapes.hashCode()) * 31) +
                colors.hashCode()) * 31 +
                logo.hashCode()) * 31 +
                errorCorrectionLevel.hashCode()) * 31 +
                fourEyed.hashCode()
    }

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

