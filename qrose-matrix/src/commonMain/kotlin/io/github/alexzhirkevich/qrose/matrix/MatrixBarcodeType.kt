package io.github.alexzhirkevich.qrose.matrix

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor

public enum class MatrixBarcodeType {
    DataMatrix,
    Aztec
}

/**
 * Remember generic 2D matrix barcode painter.
 */
@Composable
public fun rememberMatrixBarcodePainter(
    data: String,
    type: MatrixBarcodeType,
    brush: Brush = SolidColor(Color.Black),
    backgroundBrush: Brush? = null,
    pixelShape: MatrixPixelShape = MatrixPixelShape.Default,
    quietZone: Int = if (type == MatrixBarcodeType.DataMatrix) 1 else 0,
): MatrixBarcodePainter = when (type) {
    MatrixBarcodeType.DataMatrix -> rememberDataMatrixPainter(
        data = data,
        brush = brush,
        backgroundBrush = backgroundBrush,
        pixelShape = pixelShape,
        quietZone = quietZone
    )
    MatrixBarcodeType.Aztec -> rememberAztecPainter(
        data = data,
        brush = brush,
        backgroundBrush = backgroundBrush,
        pixelShape = pixelShape,
        quietZone = quietZone
    )
}
