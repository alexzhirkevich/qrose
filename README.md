# QRose 

[![Maven Central](https://maven-badges.herokuapp.com/maven-central/io.github.alexzhirkevich/qrose/badge.svg)](https://maven-badges.herokuapp.com/maven-central/io.github.alexzhirkevich/qrose)


![badge-Android](https://img.shields.io/badge/Platform-Android-brightgreen)
![badge-iOS](https://img.shields.io/badge/Platform-iOS-lightgray)
![badge-JVM](https://img.shields.io/badge/Platform-JVM-orange)
![badge-macOS](https://img.shields.io/badge/Platform-macOS-purple)
![badge-web](https://img.shields.io/badge/Platform-Web-blue)

QR code design library for Compose Multiplatform


<img width="465" alt="Screenshot 2023-10-10 at 10 34 05" src="https://github.com/alexzhirkevich/qrose/assets/63979218/7469cc1c-d6fd-4dab-997d-f2604dfa49de">

Why QRose?
- **Lightweight** - doesn't contain any dependencies except of `compose.ui`;
- **Flexible** - high customization ability that is open for extension;
- **Efficient** - declare and render codes synchronously right from the composition in 60+ fps;
- **Scalable** - no raster bitmaps, only scalable vector graphics;
- **Multiplatform** - supports all the targets supported by Compose Multiplatform.

# Installation
```gradle
dependencies {
    implementation("io.github.alexzhirkevich:qrose:1.0.0-beta02")
}
```

# Usage

You can create code right in composition using `rememberQrCodePainter`.
Or do it outside of Compose scope by instantiating a `QrCodePainter` class.

There are some overloads of `rememberQrCodePainter` including DSL constructor:
```kotlin
val logoPainter = painterResource("logo.png")

val qrcodePainter = rememberQrCodePainter("https://example.com") {
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

# Customization

You can create your own shapes for each QR code part, for example:

```kotlin
class MyCircleBallShape : QrBallShape {
    
    override fun Path.path(size: Float, neighbors: Neighbors): Path = apply {
        addOval(Rect(0f,0f, size, size))
    }
}
```
