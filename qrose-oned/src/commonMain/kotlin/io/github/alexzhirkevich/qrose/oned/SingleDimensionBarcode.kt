package io.github.alexzhirkevich.qrose.oned

import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.util.fastForEachIndexed
import io.github.alexzhirkevich.qrose.CachedPainter

typealias BarcodePathBuilder = (size : Size, code : List<Boolean>) -> Path

sealed class SingleDimensionBarcodePainter(
    density: Float,
    private val code : List<Boolean>,
    private val brush: Brush,
    private val builder : BarcodePathBuilder
) : CachedPainter(){

    override fun DrawScope.onCache() {
        drawPath(
            path = builder(size, code),
            brush = brush
        )
    }

    override val intrinsicSize: Size = Size(
        width = density * 3 * code.size,
        height = 60 * density
    )
}

@PublishedApi
internal fun defaultOneDBarcodeBuilder(size : Size, data : List<Boolean>): Path = Path().apply {

    val width = size.width / data.size

    data.fastForEachIndexed { i, b ->
        if (b) {
            addRect(
                Rect(
                    left = i * width,
                    top = 0f,
                    right = (i + 1) * width,
                    bottom = size.height
                )
            )
        }
    }
}