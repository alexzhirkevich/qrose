package io.github.alexzhirkevich.qrose

import androidx.compose.ui.graphics.Path

interface ShapeModifier {
    /**
     * Modify current path or create new one.
     * */
    public fun Path.path(size : Float, neighbors: Neighbors) : Path
}