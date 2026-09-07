package io.github.alexzhirkevich.qrose.matrix

import androidx.compose.runtime.Immutable
import io.github.alexzhirkevich.qrose.Neighbors

/**
 * 2-dimensional boolean matrix representing barcode modules.
 * `true` means a dark (foreground) module, `false` means a light (background) module.
 */
@Immutable
public class Matrix2D(
    public val width: Int,
    public val height: Int
) {
    private val bits: BooleanArray = BooleanArray(width * height)

    public operator fun get(x: Int, y: Int): Boolean {
        require(x in 0 until width && y in 0 until height) {
            "Coordinates ($x, $y) out of bounds for matrix size ${width}x${height}"
        }
        return bits[y * width + x]
    }

    public operator fun set(x: Int, y: Int, value: Boolean) {
        require(x in 0 until width && y in 0 until height) {
            "Coordinates ($x, $y) out of bounds for matrix size ${width}x${height}"
        }
        bits[y * width + x] = value
    }

    public fun copy(): Matrix2D {
        val copy = Matrix2D(width, height)
        bits.copyInto(copy.bits)
        return copy
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is Matrix2D) return false
        if (width != other.width || height != other.height) return false
        return bits.contentEquals(other.bits)
    }

    override fun hashCode(): Int {
        var result = width
        result = 31 * result + height
        result = 31 * result + bits.contentHashCode()
        return result
    }

    override fun toString(): String = buildString {
        append("Matrix2D(${width}x${height}):\n")
        for (y in 0 until height) {
            for (x in 0 until width) {
                append(if (this@Matrix2D[x, y]) "██" else "  ")
            }
            append("\n")
        }
    }
}


internal fun Matrix2D.neighbors(i : Int, j : Int) : Neighbors {

    fun cmp(i2 : Int, j2 : Int) = kotlin.runCatching {
        this[i2,j2] == this[i,j]
    }.getOrDefault(false)

    return Neighbors(
        topLeft = cmp(i - 1, j - 1),
        topRight = cmp(i + 1, j - 1),
        left = cmp(i - 1, j),
        top = cmp(i, j - 1),
        right = cmp(i + 1, j),
        bottomLeft = cmp(i - 1, j + 1),
        bottom = cmp(i, j + 1),
        bottomRight = cmp(i + 1, j + 1)
    )
}

