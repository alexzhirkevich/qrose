package io.github.alexzhirkevich.qrose.example.desktop

import App
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState

fun main() {
    application {
        val windowState = rememberWindowState()

        Window(
            onCloseRequest = ::exitApplication,
            title = "QRose example",
            state = windowState,
        ) {

           App()
        }
    }
}