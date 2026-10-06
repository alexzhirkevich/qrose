# QRose

![badge-Android](https://img.shields.io/badge/Platform-Android-brightgreen)
![badge-iOS](https://img.shields.io/badge/Platform-iOS-lightgray)
![badge-JVM](https://img.shields.io/badge/Platform-JVM-orange)
![badge-macOS](https://img.shields.io/badge/Platform-macOS-purple)
![badge-web](https://img.shields.io/badge/Platform-Web-blue)

A pure-Kotlin barcode generation library with a customizable graphical implementation for Compose Multiplatform

<img width="465" alt="Screenshot 2023-10-10 at 10 34 05" src="https://github.com/alexzhirkevich/qrose/assets/63979218/7469cc1c-d6fd-4dab-997d-f2604dfa49de">

Why QRose?
- **Lightweight** - doesn't bring any dependencies except of `compose.ui`.
- **Multiformat** - multiple formats supported: `QR`, `Data Matrix`, `Aztec`, `PDF417`, `UPC`, `EAN`, `Code 128/93/39`, `Codabar`, `ITF`.
- **Multiplatform** - Encoders are available **for each** Kotlin target. Graphical components are available for all targets supported by the Compose Multiplatform. 
- **Flexible** - high customization ability that is open for extension.
- **Efficient** - declare and render codes synchronously right from the composition in 60+ fps.
- **Scalable** - no raster bitmaps, only scalable vector graphics.

# Installation

[![Maven Central](https://img.shields.io/maven-central/v/io.github.alexzhirkevich/qrose)](https://central.sonatype.com/artifact/io.github.alexzhirkevich/qrose)  

```toml
[versions]
qrose="<version>"

[libraries]
# For QR code painter
qrose-qr = { module = "io.github.alexzhirkevich:qrose", version.ref = "qrose" }
# For 2D matrix & stacked code painters (Data Matrix, Aztec, PDF417)
qrose-matrix = { module = "io.github.alexzhirkevich:qrose-matrix", version.ref = "qrose" }
# For single-dimension barcode painters (UPC, EAN, Code128, ...)
qrose-oned = { module = "io.github.alexzhirkevich:qrose-oned", version.ref = "qrose" }
```

Encoder modules do additionally support all K/native targets and Node

```toml
# For matrix encoders without Compose (optional, included in qrose-matrix)
qrose-encoder-matrix = { module = "io.github.alexzhirkevich:qrose-encoder-matrix", version.ref = "qrose" }
# For barcode encoders without Compose (optional, included in qrose-oned)
qrose-encoder-oned = { module = "io.github.alexzhirkevich:qrose-encoder-oned", version.ref = "qrose" }
```

# Usage

- [Basic](#basic)
- [Design](#design)
- [Customize (extend)](#customize)
- [Data Types](#data-types)
- [Export Image](#export)
- [Encoders (without Compose)](#encoders)

## Basic

You can create code right in composition using `rememberQrCodePainter`, `rememberDataMatrixPainter`, `rememberAztecPainter`, `rememberPdf417Painter`, `rememberBarcodePainter`.
Or use `QrCodePainter`, `DataMatrixPainter`, `AztecPainter`, `Pdf417Painter`, `BarcodePainter` to create it outside of Compose. 

```kotlin
Image(
    painter = rememberQrCodePainter("https://example.com"),
    contentDescription = "QR code referring to the example.com website"
)

Image(
    painter = rememberDataMatrixPainter("https://example.com"),
    contentDescription = "Data Matrix code"
)

Image(
    painter = rememberAztecPainter("https://example.com"),
    contentDescription = "Aztec code"
)

Image(
    painter = rememberPdf417Painter("https://example.com"),
    contentDescription = "PDF417 barcode"
)

Image(
    painter = rememberBarcodePainter("9780201379624", BarcodeType.EAN13),
    contentDescription = "EAN barcode for some product"
)
```

## Design

QR codes have flexible styling options, for example:

```kotlin
val qrcodePainter = rememberQrCodePainter(
    data = "https://example.com",
    ballShape = QrBallShape.circle(),
    darkPixelShape = QrPixelShape.roundCorners(),
    frameShape = QrFrameShape.roundCorners(.25f),
    darkBrush = QrBrush.brush { size ->
        Brush.linearGradient(
            0f to Color.Red,
            1f to Color.Blue,
            end = Offset(size, size)
        )
    },
    frameBrush = QrBrush.solid(Color.Black),
    logoPainter = painterResource(Res.drawable.logo),
    logoPadding = QrLogoPadding.Natural(.1f),
    logoShape = QrLogoShape.circle(),
    logoSize = 0.2f,
)
```

Or with DSL constructor:


```kotlin
val logoPainter : Painter = painterResource(Res.drawable.logo)

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
        dark = QrBrush.brush { size ->
            Brush.linearGradient(
                0f to Color.Red,
                1f to Color.Blue,
                end = Offset(size, size)
            )
        }
        frame = QrBrush.solid(Color.Black)
    }
}
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

## Encoders

Using `qrose-encoder-matrix` and `qrose-encoder-oned` modules you can get barcode bit 
matrices/arrays without Compose graphical implementation.

The `QroseEncoders` object is used as an encoder factory for all types of codes. 
You can create encoders using the extension functions, for example:

```kotlin
/**
 * Creates a [MatrixCodeEncoder] that generates QR codes.
 */
fun QroseEncoders.QR(
    errorCorrection: QrErrorCorrection = QrErrorCorrection.L,
    maskPattern: QrMaskPattern = QrMaskPattern.PATTERN000
) : MatrixCodeEncoder

/**
 * Creates a [BarcodeEncoder] that generates Code 128 barcodes.
 */
fun QroseEncoders.Code128(
    compact : Boolean = true,
    forceCodeSet : Code128Type? = null
) : BarcodeEncoder
```

```kotlin
val encoder : MatrixCodeEncoder = QroseEncoders.QR()
val code : Matrix2D = encoder.encode("https://example.com")
```

