@file:OptIn(ExperimentalResourceApi::class)

package io.github.alexzhirkevich.shared
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
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
import io.github.alexzhirkevich.qrose.options.QrBallShape
import io.github.alexzhirkevich.qrose.options.QrBrush
import io.github.alexzhirkevich.qrose.options.QrCodeShape
import io.github.alexzhirkevich.qrose.options.QrErrorCorrectionLevel
import io.github.alexzhirkevich.qrose.options.QrFrameShape
import io.github.alexzhirkevich.qrose.options.QrLogoPadding
import io.github.alexzhirkevich.qrose.options.QrLogoShape
import io.github.alexzhirkevich.qrose.options.QrPixelShape
import io.github.alexzhirkevich.qrose.options.brush
import io.github.alexzhirkevich.qrose.options.circle
import io.github.alexzhirkevich.qrose.options.hexagon
import io.github.alexzhirkevich.qrose.options.image
import io.github.alexzhirkevich.qrose.options.roundCorners
import io.github.alexzhirkevich.qrose.options.solid
import io.github.alexzhirkevich.qrose.rememberQrCodePainter
import io.github.alexzhirkevich.qrose.toByteArray
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import qrose.example.shared.generated.resources.Res
import qrose.example.shared.generated.resources.jc
import qrose.example.shared.generated.resources.jcbg

@Composable
fun App() {
    AllBarcodes()
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
fun QrCode(){
    var text by remember {
        mutableStateOf("https://github.com/alexzhirkevich/qrose")
    }

    val bg = painterResource(Res.drawable.jcbg)

    val logo = painterResource(Res.drawable.jc)

    val painter = rememberQrCodePainter(text) {
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
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painter,
            contentDescription = null,
            modifier = Modifier
                .size(350.dp)
                .padding(10.dp)
        )

        TextField(
            value = text,
            onValueChange = { text = it }
        )
    }

    QrData.email(
        email = "example@mail.com",
        subject = "Mail Subject"
    )
}

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
