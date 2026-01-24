package io.github.alexzhirkevich.qrose.options.dsl

import io.github.alexzhirkevich.qrose.options.QrBrush

/**
 * Colors of QR code elements
 *
 * @property dark Brush of the dark QR code pixels
 * @property light Brush of the light QR code pixels
 * @property ball Brush of the QR code eye balls
 * @property frame Brush of the QR code eye frames
 */
public sealed interface QrColorsBuilderScope {
    public var dark: QrBrush
    public var light: QrBrush
    public var frame: QrBrush
    public var ball: QrBrush
}
