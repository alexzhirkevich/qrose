@file:OptIn(ExperimentalComposeUiApi::class)

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import io.github.alexzhirkevich.shared.App
import org.jetbrains.skiko.wasm.onWasmReady

fun main(){
    onWasmReady {
        CanvasBasedWindow {
            App()
        }
    }
}