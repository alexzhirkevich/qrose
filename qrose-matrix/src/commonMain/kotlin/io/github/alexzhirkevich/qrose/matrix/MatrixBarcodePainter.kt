package io.github.alexzhirkevich.qrose.matrix

import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import io.github.alexzhirkevich.qrose.CachedPainter
import kotlin.math.min

/**
 * Base [CachedPainter] for rendering a 2D matrix barcode.
 */
@Immutable
public open class MatrixBarcodePainter(
    public val matrix: Matrix2D,
    public val brush: Brush = SolidColor(Color.Black),
    public val backgroundBrush: Brush? = null,
    public val pixelShape: MatrixPixelShape = MatrixPixelShape.Default,
    public val quietZone: Int = 0,
) : CachedPainter() {

    override val intrinsicSize: Size = Size(
        (matrix.width + quietZone * 2) * 10f,
        (matrix.height + quietZone * 2) * 10f
    )

    override fun DrawScope.onCache() {
        val totalCols = matrix.width + quietZone * 2
        val totalRows = matrix.height + quietZone * 2

        val moduleWidth = size.width / totalCols
        val moduleHeight = size.height / totalRows
        val moduleSize = min(moduleWidth, moduleHeight)

        val actualWidth = moduleSize * totalCols
        val actualHeight = moduleSize * totalRows
        val offsetX = (size.width - actualWidth) / 2f
        val offsetY = (size.height - actualHeight) / 2f

        if (backgroundBrush != null) {
            drawRect(
                brush = backgroundBrush,
                topLeft = Offset(offsetX, offsetY),
                size = Size(actualWidth, actualHeight)
            )
        }

        val path = Path()
        val tmpPath = Path()
        for (y in 0 until matrix.height) {
            for (x in 0 until matrix.width) {
                if (matrix[x, y]) {

                    with(pixelShape) {
                        tmpPath.rewind()
                        path.addPath(
                            path = tmpPath.path(
                                size = moduleSize,
                                neighbors = matrix.neighbors(x,y)
                            ),
                            offset = Offset(
                                x = offsetX + (x + quietZone) * moduleSize,
                                y = offsetY + (y + quietZone) * moduleSize
                            )
                        )
                    }
                }
            }
        }

        drawPath(path = path, brush = brush)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is MatrixBarcodePainter) return false
        if (matrix != other.matrix) return false
        if (brush != other.brush) return false
        if (backgroundBrush != other.backgroundBrush) return false
        if (pixelShape != other.pixelShape) return false
        if (quietZone != other.quietZone) return false
        return true
    }

    override fun hashCode(): Int {
        var result = matrix.hashCode()
        result = 31 * result + brush.hashCode()
        result = 31 * result + (backgroundBrush?.hashCode() ?: 0)
        result = 31 * result + pixelShape.hashCode()
        result = 31 * result + quietZone
        return result
    }
}
