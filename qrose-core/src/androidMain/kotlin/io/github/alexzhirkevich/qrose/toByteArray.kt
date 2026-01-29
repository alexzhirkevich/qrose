package io.github.alexzhirkevich.qrose

import android.graphics.Bitmap
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.painter.BitmapPainter
import java.io.ByteArrayOutputStream


public actual fun ImageBitmap.toByteArray(
    format: ImageFormat
): ByteArray {

    return ByteArrayOutputStream().use {
        asAndroidBitmap().compress(format.toAndroid(), 100, it)
        it.toByteArray()
    }
}


private fun ImageFormat.toAndroid() = when(this){
    ImageFormat.PNG -> Bitmap.CompressFormat.PNG
    ImageFormat.JPEG -> Bitmap.CompressFormat.JPEG
    ImageFormat.WEBP -> Bitmap.CompressFormat.WEBP
}