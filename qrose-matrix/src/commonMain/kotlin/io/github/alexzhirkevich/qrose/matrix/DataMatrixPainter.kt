package io.github.alexzhirkevich.qrose.matrix

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import io.github.alexzhirkevich.qrose.matrix.datamatrix.DataMatrixEncoder
import io.github.alexzhirkevich.qrose.matrix.datamatrix.DataMatrixShape

/**
 * Remember Data Matrix barcode painter.
 *
 * @param data payload string
 * @param shape shape hint ([DataMatrixShape.Auto], [DataMatrixShape.Square], [DataMatrixShape.Rectangle])
 * @param brush module brush (solid color or gradient)
 * @param backgroundBrush optional background brush
 * @param pixelShape module shape ([MatrixPixelShape.Default], [MatrixPixelShape.RoundCorners], [MatrixPixelShape.Circle])
 * @param quietZone margin around the code in module units (recommended at least 1)
 */
@Composable
public fun rememberDataMatrixPainter(
    data: String,
    shape: DataMatrixShape = DataMatrixShape.Auto,
    brush: Brush = SolidColor(Color.Black),
    backgroundBrush: Brush? = null,
    pixelShape: MatrixPixelShape = MatrixPixelShape.Default,
    quietZone: Int = 1,
): DataMatrixPainter = remember(data, shape, brush, backgroundBrush, pixelShape, quietZone) {
    DataMatrixPainter(
        data = data,
        shape = shape,
        brush = brush,
        backgroundBrush = backgroundBrush,
        pixelShape = pixelShape,
        quietZone = quietZone
    )
}

/**
 * [MatrixBarcodePainter] for Data Matrix (ECC 200) codes.
 */
@Immutable
public class DataMatrixPainter(
    public val data: String,
    public val shape: DataMatrixShape = DataMatrixShape.Auto,
    brush: Brush = SolidColor(Color.Black),
    backgroundBrush: Brush? = null,
    pixelShape: MatrixPixelShape = MatrixPixelShape.Default,
    quietZone: Int = 1,
) : MatrixBarcodePainter(
    matrix = DataMatrixEncoder.encode(data, shape),
    brush = brush,
    backgroundBrush = backgroundBrush,
    pixelShape = pixelShape,
    quietZone = quietZone
) {
    override fun toString(): String = "DataMatrixPainter(data=$data, shape=$shape)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is DataMatrixPainter) return false
        if (data != other.data) return false
        if (shape != other.shape) return false
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + data.hashCode()
        result = 31 * result + shape.hashCode()
        return result
    }
}
