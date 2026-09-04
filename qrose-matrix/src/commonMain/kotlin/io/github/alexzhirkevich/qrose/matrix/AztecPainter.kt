package io.github.alexzhirkevich.qrose.matrix

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import io.github.alexzhirkevich.qrose.matrix.aztec.AztecEncoder

/**
 * Remember Aztec barcode painter.
 *
 * @param data payload string
 * @param errorCorrectionPercent minimum error correction percentage (default 33)
 * @param userSpecifiedLayers optional explicit layer count (positive for full, negative for compact, 0 for auto)
 * @param brush module brush (solid color or gradient)
 * @param backgroundBrush optional background brush
 * @param pixelShape module shape ([MatrixPixelShape.Default], [MatrixPixelShape.RoundCorners], [MatrixPixelShape.Circle])
 * @param quietZone margin around the code in module units (Aztec standard specifies 0 quiet zone)
 */
@Composable
public fun rememberAztecPainter(
    data: String,
    errorCorrectionPercent: Int = AztecEncoder.DEFAULT_EC_PERCENT,
    userSpecifiedLayers: Int = AztecEncoder.DEFAULT_LAYERS,
    brush: Brush = SolidColor(Color.Black),
    backgroundBrush: Brush? = null,
    pixelShape: MatrixPixelShape = MatrixPixelShape.Default,
    quietZone: Int = 0,
): AztecPainter = remember(data, errorCorrectionPercent, userSpecifiedLayers, brush, backgroundBrush, pixelShape, quietZone) {
    AztecPainter(
        data = data,
        errorCorrectionPercent = errorCorrectionPercent,
        userSpecifiedLayers = userSpecifiedLayers,
        brush = brush,
        backgroundBrush = backgroundBrush,
        pixelShape = pixelShape,
        quietZone = quietZone
    )
}

/**
 * [MatrixBarcodePainter] for Aztec codes.
 */
@Immutable
public class AztecPainter(
    public val data: String,
    public val errorCorrectionPercent: Int = AztecEncoder.DEFAULT_EC_PERCENT,
    public val userSpecifiedLayers: Int = AztecEncoder.DEFAULT_LAYERS,
    brush: Brush = SolidColor(Color.Black),
    backgroundBrush: Brush? = null,
    pixelShape: MatrixPixelShape = MatrixPixelShape.Default,
    quietZone: Int = 0,
) : MatrixBarcodePainter(
    matrix = AztecEncoder.encode(data, errorCorrectionPercent, userSpecifiedLayers),
    brush = brush,
    backgroundBrush = backgroundBrush,
    pixelShape = pixelShape,
    quietZone = quietZone
) {
    override fun toString(): String = "AztecPainter(data=$data, ecPercent=$errorCorrectionPercent)"

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is AztecPainter) return false
        if (data != other.data) return false
        if (errorCorrectionPercent != other.errorCorrectionPercent) return false
        if (userSpecifiedLayers != other.userSpecifiedLayers) return false
        return super.equals(other)
    }

    override fun hashCode(): Int {
        var result = super.hashCode()
        result = 31 * result + data.hashCode()
        result = 31 * result + errorCorrectionPercent.hashCode()
        result = 31 * result + userSpecifiedLayers.hashCode()
        return result
    }
}
