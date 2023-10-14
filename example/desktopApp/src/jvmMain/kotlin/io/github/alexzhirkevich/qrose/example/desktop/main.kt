package io.github.alexzhirkevich.qrose.example.desktop

//import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import io.github.alexzhirkevich.shared.App

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