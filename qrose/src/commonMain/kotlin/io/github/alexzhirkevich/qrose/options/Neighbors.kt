package io.github.alexzhirkevich.qrose.options

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable


/**
 * Status of the neighbor QR code pixels or eyes
 * */

@Immutable
public class Neighbors(
    public val topLeft : Boolean = false,
    public val topRight : Boolean = false,
    public val left : Boolean = false,
    public val top : Boolean = false,
    public val right : Boolean = false,
    public val bottomLeft: Boolean = false,
    public val bottom: Boolean = false,
    public val bottomRight: Boolean = false,
) {

    public companion object {
        public val Empty: Neighbors = Neighbors()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as Neighbors

        if (topLeft != other.topLeft) return false
        if (topRight != other.topRight) return false
        if (left != other.left) return false
        if (top != other.top) return false
        if (right != other.right) return false
        if (bottomLeft != other.bottomLeft) return false
        if (bottom != other.bottom) return false
        if (bottomRight != other.bottomRight) return false

        return true
    }

    override fun hashCode(): Int {
        var result = topLeft.hashCode()
        result = 31 * result + topRight.hashCode()
        result = 31 * result + left.hashCode()
        result = 31 * result + top.hashCode()
        result = 31 * result + right.hashCode()
        result = 31 * result + bottomLeft.hashCode()
        result = 31 * result + bottom.hashCode()
        result = 31 * result + bottomRight.hashCode()
        return result
    }
}
public val Neighbors.hasAny : Boolean
    get() = topLeft || topRight || left || top ||
            right || bottomLeft || bottom || bottomRight

public val Neighbors.hasAllNearest
    get() = top && bottom && left && right

public val Neighbors.hasAll : Boolean
    get() = topLeft && topRight && left && top &&
            right && bottomLeft && bottom && bottomRight

