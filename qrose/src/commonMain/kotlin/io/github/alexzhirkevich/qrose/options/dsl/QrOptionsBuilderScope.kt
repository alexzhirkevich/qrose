package io.github.alexzhirkevich.qrose.options.dsl

import io.github.alexzhirkevich.qrose.DelicateQRoseApi
import io.github.alexzhirkevich.qrose.options.QrErrorCorrectionLevel
import io.github.alexzhirkevich.qrose.options.QrErrorCorrectionLevel.Auto

public sealed interface QrOptionsBuilderScope  {

    /**
     * Level of error correction.
     * [Auto] by default
     * */
    public var errorCorrectionLevel: QrErrorCorrectionLevel

    /**
     * Enable 4th qr code eye. False by default
     * */
    @DelicateQRoseApi
    public var fourEyed : Boolean

    /**
     * Shapes of the QR code pattern and its parts.
     * */
    public fun shapes(centralSymmetry : Boolean = true, block: QrShapesBuilderScope.() -> Unit)

    /**
     * Colors of QR code parts.
     * */
    public fun colors(block: QrColorsBuilderScope.() -> Unit)

    /**
     * Middle image.
     * */
    public fun logo(block: QrLogoBuilderScope.() -> Unit)
}




