@file:OptIn(ExperimentalComposeUiApi::class)

import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.window.CanvasBasedWindow
import io.github.alexzhirkevich.shared.App

@OptIn(ExperimentalComposeUiApi::class)
fun main() {
    CanvasBasedWindow {
        App()
    }
}