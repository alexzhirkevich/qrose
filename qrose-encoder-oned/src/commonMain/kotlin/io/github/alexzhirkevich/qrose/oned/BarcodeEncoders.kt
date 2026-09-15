package io.github.alexzhirkevich.qrose.oned

import io.github.alexzhirkevich.qrose.QroseEncoders

/**
 * Creates a [BarcodeEncoder] that generates Codabar barcodes.
 */
fun QroseEncoders.Codabar() : BarcodeEncoder = CodabarEncoder

/**
 * Creates a [BarcodeEncoder] that generates Code 39 barcodes.
 */
fun QroseEncoders.Code39() : BarcodeEncoder = Code39Encoder

/**
 * Creates a [BarcodeEncoder] that generates Code 93 barcodes.
 */
fun QroseEncoders.Code93() : BarcodeEncoder = Code93Encoder

/**
 * Creates a [BarcodeEncoder] that generates Code 128 barcodes.
 *
 * @param compact whether to prefer a more compact encoding by choosing code sets that
 * minimize the resulting barcode length. Defaults to `true`.
 * @param forceCodeSet forces encoding to use a specific [Code128Type] code set instead of
 * letting the encoder choose automatically. Defaults to `null`, letting the encoder decide.
 */
fun QroseEncoders.Code128(
    compact : Boolean = true,
    forceCodeSet : Code128Type? = null
) : BarcodeEncoder = Code128Encoder(compact, forceCodeSet)

/**
 * Creates a [BarcodeEncoder] that generates EAN-8 barcodes.
 */
fun QroseEncoders.EAN8() : BarcodeEncoder = CodeEAN8Encoder

/**
 * Creates a [BarcodeEncoder] that generates EAN-13 barcodes.
 */
fun QroseEncoders.EAN13() : BarcodeEncoder = CodeEAN13Encoder

/**
 * Creates a [BarcodeEncoder] that generates ITF (Interleaved 2 of 5) barcodes.
 */
fun QroseEncoders.ITF() : BarcodeEncoder = CodeITFEncoder

/**
 * Creates a [BarcodeEncoder] that generates UPC-A barcodes.
 */
fun QroseEncoders.UPCA() : BarcodeEncoder = CodeUPCAEncoder

/**
 * Creates a [BarcodeEncoder] that generates UPC-E barcodes.
 */
fun QroseEncoders.UPCE() : BarcodeEncoder = CodeUPCEEncoder