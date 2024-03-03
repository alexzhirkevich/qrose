# QRose

![badge-Android](https://img.shields.io/badge/Platform-Android-brightgreen)
![badge-iOS](https://img.shields.io/badge/Platform-iOS-lightgray)
![badge-JVM](https://img.shields.io/badge/Platform-JVM-orange)
![badge-macOS](https://img.shields.io/badge/Platform-macOS-purple)
![badge-web](https://img.shields.io/badge/Platform-Web-blue)

Barcode generation library for Compose Multiplatform

<img width="465" alt="Screenshot 2023-10-10 at 10 34 05" src="https://github.com/alexzhirkevich/qrose/assets/63979218/7469cc1c-d6fd-4dab-997d-f2604dfa49de">

Why QRose?
- **Lightweight** - doesn't contain any dependencies except of `compose.ui`;
- **Flexible** - high customization ability that is open for extension;
- **Efficient** - declare and render codes synchronously right from the composition in 60+ fps;
- **Scalable** - no raster bitmaps, only scalable vector graphics;
- **Multiplatform** - supports all the targets supported by Compose Multiplatform.
- **Multiformat** - multiple formats supported: `QR`, `UPC`, `EAN`, `Code 128/93/39`, `Codabar`, `ITF`.

# Installation

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.alexzhirkevich/qrose/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.alexzhirkevich/qrose)

```gradle
dependencies {

    // For QR codes
    implementation("io.github.alexzhirkevich:qrose:1.0.0")
    
    // For single-dimension barcodes (UPC,EAN, Code128, ...)
    implementation("io.github.alexzhirkevich:qrose-oned:1.0.0")
}
```

# Usage

## Basic

You can create code right in composition using `rememberQrCodePainter`, `rememberBarcodePainter`.
Or use `QrCodePainter`, `BarcodePainter` to create it outside of Compose. 

```kotlin
Image(
    painter = rememberQrCodePainter("https://example.com"),
    contentDescription = "QR code referring to the example.com website"
)

Image(
    painter = rememberBarcodePainter("9780201379624", BarcodeType.EAN13),
    contentDescription = "EAN barcode for some product"
)
```

## Design

QR codes have flexible styling options, for example:

```kotlin
val logoPainter : Painter = painterResource("logo.png")

val qrcodePainter : Painter = rememberQrCodePainter("https://example.com") {
    logo {
        painter = logoPainter
        padding = QrLogoPadding.Natural(.1f)
        shape = QrLogoShape.circle()
        size = 0.2f
    }

    shapes {
        ball = QrBallShape.circle()
        darkPixel = QrPixelShape.roundCorners()
        frame = QrFrameShape.roundCorners(.25f)
    }
    colors {
        dark = QrBrush.brush {
            Brush.linearGradient(
                0f to Color.Red,
                1f to Color.Blue,
                end = Offset(it, it)
            )
        }
        frame = QrBrush.solid(Color.Black)
    }
}
```

Or just parametrized constructor:

```kotlin
val qrcodePainter = rememberQrCodePainter(
    data = "https://example.com",
    shapes = QrShapes(
        darkPixel = QrPixelShape.roundCorners()
    )
)
```

## Customize

You can create your own shapes for each QR code part, for example:

```kotlin
class MyCircleBallShape : QrBallShape {
    
    override fun Path.path(size: Float, neighbors: Neighbors): Path = apply {
        addOval(Rect(0f,0f, size, size))
    }
}
```

> **Note**
>A path here uses [`PathFillType.EvenOdd`](https://developer.android.com/reference/kotlin/androidx/compose/ui/graphics/PathFillType#EvenOdd()) that cannot be changed.

## Data types

QR codes can hold various payload types: Text, Wi-Fi, E-mail, vCard, etc.

`QrData` object can be used to perform such encodings, for example:

```kotlin
val wifiData : String = QrData.wifi(ssid = "My Network", psk = "12345678")

val wifiCode = rememberQrCodePainter(wifiData)
```

## Export

QR codes can be exported to `PNG`, `JPEG` and `WEBP` formats using `toByteArray` function:

```kotlin

val painter : Painter = QrCodePainter(
    data = "https://example.com",
    options =  QrOptions { 
        colors {
            //...
        }
    }
)

val bytes : ByteArray = painter.toByteArray(1024, 1024, ImageFormat.PNG)
```
