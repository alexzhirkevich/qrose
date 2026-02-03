package io.github.alexzhirkevich.qrose.qrcode.internals

import io.github.alexzhirkevich.qrose.qrcode.internals.QRCodeRegion.BOTTOM_LEFT_CORNER
import io.github.alexzhirkevich.qrose.qrcode.internals.QRCodeRegion.BOTTOM_RIGHT_CORNER
import io.github.alexzhirkevich.qrose.qrcode.internals.QRCodeRegion.TOP_LEFT_CORNER
import io.github.alexzhirkevich.qrose.qrcode.internals.QRCodeRegion.TOP_RIGHT_CORNER

/**
 * Represents a single QRCode square unit. It has information about its "color" (either dark or bright),
 * its position (row and column) and what it represents.
 *
 * It can be part of a position probe (aka those big squares at the extremities), part of a position
 * adjustment square, part of a timing pattern or just another square as any other :)
 *
 * @author Rafael Lins - g0dkar
 */
internal class QRCodeSquare(
    /** Is this a painted square? */
    var dark: Boolean
)

/**
 * Represents which part/region of a given square type a particular, single square is.
 *
 * For example, a position probe is visually composed of multiple squares that form a bigger one.
 *
 * For example, this is what a position probe normally looks like (squares spaced for ease of understanding):
 *
 * ```
 * A■■■■B
 * ■ ■■ ■
 * ■ ■■ ■
 * C■■■■D
 * ```
 *
 * The positions marked with `A`, `B`, `C` and `D` would be regions [TOP_LEFT_CORNER], [TOP_RIGHT_CORNER],
 * [BOTTOM_LEFT_CORNER] and [BOTTOM_RIGHT_CORNER] respectively.
 */
internal enum class QRCodeRegion {
    TOP_LEFT_CORNER,
    TOP_RIGHT_CORNER,
    TOP_MID,
    LEFT_MID,
    RIGHT_MID,
    CENTER,
    BOTTOM_LEFT_CORNER,
    BOTTOM_RIGHT_CORNER,
    BOTTOM_MID,
    MARGIN,
    UNKNOWN
}
