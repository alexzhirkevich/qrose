package io.github.alexzhirkevich.qrose.oned

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import io.github.alexzhirkevich.qrose.QroseEncoders


/**
 * Code 128 barcode painter
 *
 * @param brush code brush
 * @param compact Specifies whether to use compact mode for Code-128 code.
 * This can yield slightly smaller bar codes. This option and [forceCodeSet] are mutually exclusive.
 * @param forceCodeSet Forces which encoding will be used. This option and [compact] are mutually exclusive.
 * @param onError called when input content is invalid.
 * @param builder build code path using painter size and encoded boolean list.
 *
 * @see Code128Painter
 * */
@Composable
internal fun rememberCode128Painter(
    data: String,
    brush: Brush = SolidColor(Color.Black),
    compact : Boolean = true,
    forceCodeSet : Code128Type? = null,
    onError : (Throwable) -> Painter = { throw  it },
    builder : BarcodePathBuilder = ::defaultBarcodeBuilder
) : Painter {

    val updatedBuilder by rememberUpdatedState(builder)

    return remember(data, brush, forceCodeSet) {
        runCatching {
            Code128Painter(
                data = data,
                brush = brush,
                compact = compact,
                codeSet = forceCodeSet,
                builder = { size, code ->
                    updatedBuilder(size, code)
                },
            )
        }.getOrElse(onError)
    }
}


@Stable
public fun Code128Painter(
    data : String,
    brush: Brush = SolidColor(Color.Black),
    compact : Boolean = true,
    codeSet : Code128Type? = null,
    builder : BarcodePathBuilder= ::defaultBarcodeBuilder
): BarcodePainter = BarcodePainter(
    code = QroseEncoders.Code128(compact, codeSet).encode(data),
    brush = brush,
    builder = builder
)
