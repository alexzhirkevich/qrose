package io.github.alexzhirkevich.qrose

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asSkiaBitmap
import org.jetbrains.skia.EncodedImageFormat
import org.jetbrains.skia.Image
import org.jetbrains.skia.impl.use


public actual fun ImageBitmap.toByteArray(
    format: ImageFormat
): ByteArray {
    val data = Image
        .makeFromBitmap(asSkiaBitmap())
        .use {
            it.encodeToData(format.toSkia())
                ?: error("This bitmap cannot be encoded to $format")
        }

    return data.bytes
}

private fun ImageFormat.toSkia() = when(this){
    ImageFormat.PNG -> EncodedImageFormat.PNG
    ImageFormat.JPEG -> EncodedImageFormat.JPEG
    ImageFormat.WEBP -> EncodedImageFormat.WEBP
}