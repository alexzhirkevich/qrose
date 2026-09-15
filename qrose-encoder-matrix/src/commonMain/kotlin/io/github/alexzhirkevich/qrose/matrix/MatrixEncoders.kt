package io.github.alexzhirkevich.qrose.matrix

import io.github.alexzhirkevich.qrose.QroseEncoders
import io.github.alexzhirkevich.qrose.matrix.aztec.AztecEncoder
import io.github.alexzhirkevich.qrose.matrix.aztec.AztecDefaultEcPrecent
import io.github.alexzhirkevich.qrose.matrix.aztec.AztecDefaultLayers
import io.github.alexzhirkevich.qrose.matrix.datamatrix.DataMatrixEncoder
import io.github.alexzhirkevich.qrose.matrix.pdf417.Pdf417Compaction
import io.github.alexzhirkevich.qrose.matrix.pdf417.Pdf417Dimensions
import io.github.alexzhirkevich.qrose.matrix.pdf417.Pdf417Encoder
import io.github.alexzhirkevich.qrose.matrix.pdf417.Pdf417ErrorCorrectionLevel
import io.github.alexzhirkevich.qrose.matrix.qr.QrEncoder
import io.github.alexzhirkevich.qrose.matrix.qr.QrErrorCorrection
import io.github.alexzhirkevich.qrose.matrix.qr.QrMaskPattern

/**
 * Creates a [MatrixCodeEncoder] that generates QR codes.
 *
 * @param errorCorrection the error correction level to use when encoding the QR code.
 * Higher levels allow the code to remain readable even if partially damaged or obscured,
 * at the cost of a denser code. Defaults to [QrErrorCorrection.L].
 * @param maskPattern the mask pattern applied to the QR code matrix to avoid patterns that
 * are hard for scanners to read. Defaults to [QrMaskPattern.PATTERN000].
 */
fun QroseEncoders.QR(
    errorCorrection: QrErrorCorrection = QrErrorCorrection.L,
    maskPattern: QrMaskPattern = QrMaskPattern.PATTERN000
) : MatrixCodeEncoder = QrEncoder(errorCorrection, maskPattern)


/**
 * Creates a [MatrixCodeEncoder] that generates Aztec codes.
 *
 * @param minEccPercent the minimum percentage of the code's data capacity reserved for
 * error correction. Higher values improve resilience to damage at the cost of a larger
 * code. Defaults to [AztecDefaultEcPrecent].
 * @param layers the number of layers to use when building the Aztec code.
 * A value of `0` (or the default) lets the encoder automatically determine the number
 * of layers needed based on the encoded data. Defaults to [AztecDefaultLayers].
 */
fun QroseEncoders.Aztec(
    minEccPercent: Int = AztecDefaultEcPrecent,
    layers: Int = AztecDefaultLayers
) : MatrixCodeEncoder = AztecEncoder(
    minEccPercent = minEccPercent,
    userSpecifiedLayers = layers
)


/**
 * Creates a [MatrixCodeEncoder] that generates DataMatrix codes.
 *
 * @param shape shape of the data matrix pattern (square or rectangular).
 * By default, chooses the smallest symbol, preferring square over rectangular if both fit
 * */
fun QroseEncoders.DataMatrix(
    shape: DataMatrixShape = DataMatrixShape.Auto
) : MatrixCodeEncoder = DataMatrixEncoder(shape)


/**
 * Creates a [MatrixCodeEncoder] that generates PDF417 codes.
 *
 * @param errorCorrectionLevel the level of error correction to embed in the code, allowing
 * it to remain readable even if partially damaged. Defaults to [Pdf417ErrorCorrectionLevel.Auto],
 * letting the encoder pick a suitable level automatically.
 * @param compaction the data compaction mode used to encode the input data. Defaults to
 * [Pdf417Compaction.Auto], letting the encoder choose the most efficient mode automatically.
 * @param dimensions bounds on the number of data columns and rows for the resulting PDF417
 * symbol. Defaults to [Pdf417Dimensions.Default].
 * @param compact whether to prefer a more compact code layout, typically by omitting the
 * stop pattern, at the possible cost of compatibility with some scanners. Defaults to `false`.
 */
fun QroseEncoders.PDF417(
    errorCorrectionLevel: Pdf417ErrorCorrectionLevel = Pdf417ErrorCorrectionLevel.Auto,
    compaction: Pdf417Compaction = Pdf417Compaction.Auto,
    dimensions: Pdf417Dimensions = Pdf417Dimensions.Default,
    compact: Boolean = false,
) : MatrixCodeEncoder = Pdf417Encoder(
    errorCorrectionLevel = errorCorrectionLevel,
    compaction = compaction,
    dimensions = dimensions,
    compact = compact
)
