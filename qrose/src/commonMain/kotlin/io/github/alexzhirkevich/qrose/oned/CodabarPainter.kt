package io.github.alexzhirkevich.qrose.oned

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity

/**
 * Codabar barcode painter
 *
 * @param brush code brush
 * @param onError called when input content is invalid
 * @param builder build code path using painter size and encoded boolean list
 * */
@Composable
fun rememberCodabarPainter(
    data: String,
    brush: Brush = SolidColor(Color.Black),
    onError : (Throwable) -> Painter = { throw  it },
    builder : BarcodePathBuilder = ::defaultOneDBarcodeBuilder
) : Painter {
    val density = LocalDensity.current

    val updatedBuilder by rememberUpdatedState(builder)

    return remember(density, data, brush) {
        runCatching {
            CodabarPainter(
                data = data,
                density = density.density,
                brush = brush,
                builder = { size, code ->
                    updatedBuilder(size, code)
                },
            )
        }.getOrElse(onError)
    }
}

class CodabarPainter(
    val data : String,
    val brush: Brush = SolidColor(Color.Black),
    density : Float = 1f,
    builder: BarcodePathBuilder = ::defaultOneDBarcodeBuilder,
) : SingleDimensionBarcodePainter(
    density = density,
    code = Codabar().encode(data).toList(),
    brush = brush,
    builder = builder
)