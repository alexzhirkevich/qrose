package io.github.alexzhirkevich.qrose.options.dsl

import io.github.alexzhirkevich.qrose.options.QrErrorCorrectionLevel
import io.github.alexzhirkevich.qrose.options.QrOptions
import io.github.alexzhirkevich.qrose.DelicateQRoseApi


internal class InternalQrOptionsBuilderScope(
    private val builder: QrOptions.Builder
) : QrOptionsBuilderScope {

    override var errorCorrectionLevel: QrErrorCorrectionLevel
        get() = builder.errorCorrectionLevel
        set(value) {
            builder.errorCorrectionLevel = value
        }

    @DelicateQRoseApi
    override var fourEyed: Boolean
        get() = builder.fourthEyeEnabled
        set(value) {
            builder.fourthEyeEnabled = value
        }

    override var scale: Float
        get() = builder.scale
        set(value) {
            builder.scale = value
        }

    override fun shapes(
        centralSymmetry : Boolean,
        block: QrShapesBuilderScope.() -> Unit
    ) {
        InternalQrShapesBuilderScope(builder,centralSymmetry).apply(block)
    }

    override fun colors(block: QrColorsBuilderScope.() -> Unit) {
        InternalQrColorsBuilderScope(builder).apply(block)
    }


    override fun logo(block: QrLogoBuilderScope.() -> Unit) {
        InternalQrLogoBuilderScope(builder).apply(block)
    }

    override fun background(block: QrBackgroundBuilderScope.() -> Unit) {
        InternalQrBackgroundBuilderScope(builder).apply(block)
    }
}