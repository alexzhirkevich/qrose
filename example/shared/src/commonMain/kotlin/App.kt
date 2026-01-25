@file:OptIn(ExperimentalResourceApi::class)

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.Crossfade
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import io.github.alexzhirkevich.qrose.DelicateQRoseApi
import io.github.alexzhirkevich.qrose.QrData
import io.github.alexzhirkevich.qrose.email
import io.github.alexzhirkevich.qrose.oned.BarcodeType
import io.github.alexzhirkevich.qrose.oned.rememberBarcodePainter
import io.github.alexzhirkevich.qrose.options.Neighbors
import io.github.alexzhirkevich.qrose.options.QrBallShape
import io.github.alexzhirkevich.qrose.options.QrBrush
import io.github.alexzhirkevich.qrose.options.QrBrushMode
import io.github.alexzhirkevich.qrose.options.QrCodeShape
import io.github.alexzhirkevich.qrose.options.QrErrorCorrectionLevel
import io.github.alexzhirkevich.qrose.options.QrFrameShape
import io.github.alexzhirkevich.qrose.options.QrLogoPadding
import io.github.alexzhirkevich.qrose.options.QrLogoShape
import io.github.alexzhirkevich.qrose.options.QrOptions
import io.github.alexzhirkevich.qrose.options.QrPixelShape
import io.github.alexzhirkevich.qrose.options.asPixel
import io.github.alexzhirkevich.qrose.options.brush
import io.github.alexzhirkevich.qrose.options.circle
import io.github.alexzhirkevich.qrose.options.hexagon
import io.github.alexzhirkevich.qrose.options.image
import io.github.alexzhirkevich.qrose.options.roundCorners
import io.github.alexzhirkevich.qrose.options.solid
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import io.github.alexzhirkevich.qrose.toByteArray
import io.github.alexzhirkevich.qrose.toImageBitmap
import io.github.alexzhirkevich.shared.generated.resources.Res
import io.github.alexzhirkevich.shared.generated.resources.jcbg
import io.github.alexzhirkevich.shared.generated.resources.jc
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource

enum class Page  {
    Generator, Scanner
}

class BrushColor(
    private val builder: (size : Float) -> Brush
) : QrBrush {

    override val mode: QrBrushMode
        get() = QrBrushMode.Separate

    override fun brush(size: Float, neighbors: Neighbors): Brush = this.builder(size)
}

@Composable
fun App() {

    return QrCode()

    var page by remember {
        mutableStateOf(Page.Scanner)
    }

    MaterialTheme(
        colorScheme = lightColorScheme()
    ) {
        Scaffold(
            bottomBar = {
                NavigationBar {
                    NavigationBarItem(
                        selected = page == Page.Generator,
                        onClick = {
                            page = Page.Generator
                        },
                        label = {
                            Text("Generator")
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Create,
                                contentDescription = null
                            )
                        }
                    )
                    NavigationBarItem(
                        selected = page == Page.Scanner,
                        onClick = {
                            page = Page.Scanner
                        },
                        label = {
                            Text("Scanner")
                        },
                        icon = {
                            Icon(
                                imageVector = Icons.Default.Search,
                                contentDescription = null
                            )
                        }
                    )
                }
            }
        ) {
            Box(Modifier.padding(it)) {
                when (page) {
                    Page.Generator -> AllBarcodes()
                    Page.Scanner -> Scanner()
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AllBarcodes() {
    FlowRow(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        OnedCode("ITF", rememberBarcodePainter("123456734512", BarcodeType.ITF))
        OnedCode("UPC E", rememberBarcodePainter("02345673", BarcodeType.UPCE))
        OnedCode("UPC A", rememberBarcodePainter("123456789012", BarcodeType.UPCA))
        OnedCode("EAN 13", rememberBarcodePainter("9780201379624", BarcodeType.EAN13))
        OnedCode("EAN 8", rememberBarcodePainter("1234567", BarcodeType.EAN8))
        OnedCode("Code 39", rememberBarcodePainter("TEST", BarcodeType.Code39))
        OnedCode("Code 93", rememberBarcodePainter("TEST", BarcodeType.Code93))
        OnedCode("Code 128", rememberBarcodePainter("test", BarcodeType.Code128))
        OnedCode("Codabar", rememberBarcodePainter("A23342453D", BarcodeType.Codabar))
        OnedCode("QR", rememberQrCodePainter("https://github.com/alexzhirkevich/qrose"))
    }
}

@Composable
fun OnedCode(
    name : String,
    code : Painter
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .padding(50.dp)
            .border(1.dp, Color.Black)
            .padding(10.dp)
    ) {
        Image(
            painter = code,
            contentDescription = null,
            modifier = Modifier
                .width(300.dp)
                .height(100.dp)
        )
        Text(name)
    }
}


@OptIn(ExperimentalResourceApi::class)
@Composable
fun QrCode() {
    var text by remember {
        mutableStateOf("https://github.com/alexzhirkevich/qrose")
    }

    val bg = painterResource(Res.drawable.jcbg)

    val logo = painterResource(Res.drawable.jc)

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        AnimatedContent(text, transitionSpec = {
            fadeIn() togetherWith fadeOut()
        }) {
            val painter = rememberQrCodePainter(it, options = QrOptions{
                logo {
                    painter = logo
                    padding = QrLogoPadding.Natural(.1f)
                    shape = QrLogoShape.circle()
                    size = 0.2f
                }

                shapes() {
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
            })
            Image(
                painter = painter,
                contentDescription = null,
                modifier = Modifier
                    .size(350.dp)
                    .padding(10.dp)
            )
        }

        TextField(
            value = text,
            onValueChange = { text = it }
        )
    }

}

@Composable
expect fun Scanner()

/**
 * This is a QR code from README
 * */
@OptIn(DelicateQRoseApi::class)
@Composable
fun JetpackCompose(data : String) : Painter {
    val bg = painterResource(Res.drawable.jcbg)
    val logo = painterResource(Res.drawable.jc)

    return rememberQrCodePainter(data) {
        fourEyed = true

        logo {
            painter = logo
            padding = QrLogoPadding.Natural(.1f)
            shape = QrLogoShape.circle()
            size = 0.125f
        }

        errorCorrectionLevel = QrErrorCorrectionLevel.MediumHigh

        shapes(centralSymmetry = true) {
            pattern = QrCodeShape.hexagon()
            ball = QrBallShape.roundCorners(.25f, bottomRight = false)
            darkPixel = QrPixelShape.roundCorners()
            frame = QrFrameShape.roundCorners(.25f, bottomRight = false)
        }
        colors {
            dark = QrBrush.image(bg,)
        }
    }
}
