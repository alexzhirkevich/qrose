package io.github.alexzhirkevich.qrose

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image

actual typealias ImageFormat = EncodedImageFormat

actual fun ImageBitmap.toByteArray(
    format: ImageFormat
): ByteArray {
    val data = Image
        .makeFromBitmap(asSkiaBitmap())
        .encodeToData(format) ?: error("This painter cannot be encoded to $format")

    return data.bytes
}