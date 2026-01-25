package io.github.alexzhirkevich.qrose.options.dsl

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter

/**
 * Background of the QR code.
 *
 * @property painter background image
 * @property fill background fill (goes behind the [painter] if specified)
 * @property shape shape of the background fill
 * */
public sealed interface QrBackgroundBuilderScope  {
    public var painter: Painter?
    public var fill : Brush?
    public var shape: Shape
}

