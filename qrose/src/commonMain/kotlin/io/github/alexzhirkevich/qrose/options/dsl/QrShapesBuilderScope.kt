package io.github.alexzhirkevich.qrose.options.dsl

import io.github.alexzhirkevich.qrose.options.QrBallShape
import io.github.alexzhirkevich.qrose.options.QrCodeShape
import io.github.alexzhirkevich.qrose.options.QrFrameShape
import io.github.alexzhirkevich.qrose.options.QrPixelShape

/**
 * Shapes of QR code elements
 *
 * @property pattern Shape of the QR code pattern.
 * @property darkPixel Shape of the dark QR code pixels
 * @property lightPixel Shape of the light QR code pixels
 * @property ball Shape of the QR code eye balls
 * @property frame Shape of the QR code eye frames
 * */
public sealed interface QrShapesBuilderScope {
    public var darkPixel: QrPixelShape
    public var lightPixel: QrPixelShape
    public var ball: QrBallShape
    public var frame: QrFrameShape
    public var pattern: QrCodeShape
}

