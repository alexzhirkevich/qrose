package io.github.alexzhirkevich.qrose.matrix

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.DrawScope
import io.github.alexzhirkevich.qrose.matrix.pdf417.Pdf417Compaction
import io.github.alexzhirkevich.qrose.matrix.pdf417.Pdf417Dimensions
import io.github.alexzhirkevich.qrose.matrix.pdf417.Pdf417Encoder
import io.github.alexzhirkevich.qrose.matrix.pdf417.Pdf417ErrorCorrectionLevel
import kotlin.math.min

/**
 * Remember PDF417 barcode painter.
 *
 * @param data payload string
 * @param errorCorrectionLevel error correction level ([Pdf417ErrorCorrectionLevel.Auto], [Pdf417ErrorCorrectionLevel.Level0]..[Pdf417ErrorCorrectionLevel.Level8])
 * @param compaction compaction mode ([Pdf417Compaction.Auto], [Pdf417Compaction.Text], [Pdf417Compaction.Byte], [Pdf417Compaction.Numeric])
 * @param dimensions column and row bounds
 * @param compact whether to generate Compact (truncated) PDF417
 * @param rowHeightRatio ratio of row height to module width (standard PDF417 is 3.0f)
 * @param brush module brush (solid color or gradient)
 * @param backgroundBrush optional background brush
 * @param quietZone margin around the code in module units (recommended at least 2)
 */
@Composable
public fun rememberPdf417Painter(
    data: String,
    errorCorrectionLevel: Pdf417ErrorCorrectionLevel = Pdf417ErrorCorrectionLevel.Auto,
    compaction: Pdf417Compaction = Pdf417Compaction.Auto,
    dimensions: Pdf417Dimensions = Pdf417Dimensions(),
    compact: Boolean = false,
    rowHeightRatio: Float = 3.0f,
    brush: Brush = SolidColor(Color.Black),
    backgroundBrush: Brush? = null,
    pixelShape: MatrixPixelShape = MatrixPixelShape.Default,
    quietZone: Int = 2,
): Pdf417Painter = remember(
    data,
    errorCorrectionLevel,
    compaction,
    dimensions,
    compact,
    rowHeightRatio,
    brush,
    backgroundBrush,
    pixelShape,
    quietZone
) {
    Pdf417Painter(
        data = data,
        errorCorrectionLevel = errorCorrectionLevel,
        compaction = compaction,
        dimensions = dimensions,
        compact = compact,
        rowHeightRatio = rowHeightRatio,
        brush = brush,
        backgroundBrush = backgroundBrush,
        pixelShape = pixelShape,
        quietZone = quietZone
    )
}

/**
 * [MatrixBarcodePainter] for PDF417 barcodes.
 */
@Immutable
public open class Pdf417Painter(
    matrix: Matrix2D,
    brush: Brush = SolidColor(Color.Black),
    backgroundBrush: Brush? = null,
    pixelShape: MatrixPixelShape = MatrixPixelShape.Default,
    quietZone: Int = 2,
    public val rowHeightRatio: Float = 3.0f,
) : MatrixBarcodePainter(
    matrix = matrix,
    brush = brush,
    backgroundBrush = backgroundBrush,
    pixelShape = pixelShape,
    quietZone = quietZone
) {
    public constructor(
        data: String,
        errorCorrectionLevel: Pdf417ErrorCorrectionLevel = Pdf417ErrorCorrectionLevel.Auto,
        compaction: Pdf417Compaction = Pdf417Compaction.Auto,
        dimensions: Pdf417Dimensions = Pdf417Dimensions(),
        compact: Boolean = false,
        rowHeightRatio: Float = 3.0f,
        brush: Brush = SolidColor(Color.Black),
        backgroundBrush: Brush? = null,
        pixelShape: MatrixPixelShape = MatrixPixelShape.Default,
        quietZone: Int = 2,
    ) : this(
        matrix = Pdf417Encoder.encode(
            data = data,
            errorCorrectionLevel = errorCorrectionLevel,
            compaction = compaction,
            dimensions = dimensions,
            compact = compact
        ),
        brush = brush,
        backgroundBrush = backgroundBrush,
        pixelShape = pixelShape,
        quietZone = quietZone,
        rowHeightRatio = rowHeightRatio
    )

    override val intrinsicSize: Size = Size(
        (matrix.width + quietZone * 2) * 4f,
        (matrix.height * rowHeightRatio + quietZone * 2) * 4f
    )

    override fun DrawScope.onCache() {
        val totalCols = matrix.width + quietZone * 2
        val totalHeightUnits = matrix.height * rowHeightRatio + quietZone * 2

        val moduleWidth = min(size.width / totalCols, size.height / totalHeightUnits)
        val moduleHeight = moduleWidth * rowHeightRatio

        val actualWidth = moduleWidth * totalCols
        val actualHeight = totalHeightUnits * moduleWidth
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
        for (y in 0 until matrix.height) {
            val rowY = offsetY + (quietZone + y * rowHeightRatio) * moduleWidth
            var x = 0
            while (x < matrix.width) {
                if (matrix[x, y]) {
                    var runLen = 1
                    while (x + runLen < matrix.width && matrix[x + runLen, y]) {
                        runLen++
                    }
                    val colX = offsetX + (x + quietZone) * moduleWidth
                    path.addRect(
                        Rect(
                            offset = Offset(colX, rowY),
                            size = Size(runLen * moduleWidth, moduleHeight)
                        )
                    )
                    x += runLen
                } else {
                    x++
                }
            }
        }

        drawPath(path = path, brush = brush)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Pdf417Painter) return false
        if (rowHeightRatio != other.rowHeightRatio) return false
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + rowHeightRatio.hashCode()
        return result
    }
}
