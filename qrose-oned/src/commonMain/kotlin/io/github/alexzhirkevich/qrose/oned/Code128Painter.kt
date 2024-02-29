package io.github.alexzhirkevich.qrose.oned

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.platform.LocalDensity


/**
 * Code 128 barcode painter
 *
 * @param brush code brush
 * @param compact Specifies whether to use compact mode for Code-128 code. This can yield slightly smaller bar codes. This option and [forceCodeSet] are mutually exclusive.
 * @param forceCodeSet Forces which encoding will be used. Currently only used for Code-128 code sets. This option and CODE128_COMPACT are mutually exclusive
 * @param onError called when input content is invalid
 * @param builder build code path using painter size and encoded boolean list
 * */
@Composable
fun rememberCode128Painter(
    data: String,
    brush: Brush = SolidColor(Color.Black),
    compact : Boolean = true,
    forceCodeSet : Code128Painter.CodeSet? = null,
    onError : (Throwable) -> Painter = { throw  it },
    builder : BarcodePathBuilder = ::defaultOneDBarcodeBuilder
) : Painter {

    val density = LocalDensity.current

    val updatedBuilder by rememberUpdatedState(builder)

    return remember(density, data, brush, forceCodeSet) {
        runCatching {
            Code128Painter(
                data = data,
                density = density.density,
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


@Immutable
class Code128Painter(
    val data : String,
    val brush: Brush = SolidColor(Color.Black),
    val compact : Boolean = true,
    val codeSet : CodeSet? = null,
    density: Float,
    builder : BarcodePathBuilder= ::defaultOneDBarcodeBuilder
) : SingleDimensionBarcodePainter(
    density = density,
    code = Code128().encode(data, compact, codeSet).toList(),
    brush = brush,
    builder = builder
) {

    enum class CodeSet(internal val v: Int) {
        A(Code128.CODE_CODE_A),
        B(Code128.CODE_CODE_B),
        C(Code128.CODE_CODE_C)
    }
}
