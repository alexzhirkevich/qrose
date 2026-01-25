package io.github.alexzhirkevich.qrose.options.dsl

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import io.github.alexzhirkevich.qrose.options.QrOptions

internal class InternalQrBackgroundBuilderScope(
    private val builder: QrOptions.Builder,
) : QrBackgroundBuilderScope {

    override var painter: Painter?
        get() = builder.background.painter
        set(value) = with(builder) {
            background = background.copy(painter = value)
        }

    override var fill: Brush?
        get() = builder.background.fill
        set(value) = with(builder) {
            background = background.copy(brush = value)
        }

    override var shape: Shape
        get() = builder.background.shape
        set(value) = with(builder) {
            background = background.copy(shape = value)
        }

}