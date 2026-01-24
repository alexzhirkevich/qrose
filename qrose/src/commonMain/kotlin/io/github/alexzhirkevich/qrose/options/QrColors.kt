package io.github.alexzhirkevich.qrose.options

import androidx.compose.runtime.Immutable
import androidx.compose.runtime.Stable


/**
 * Colors of QR code elements
 *
 * @param dark brush of the dark QR code pixels
 * @param light brush of the light QR code pixels
 * @param ball brush of the QR code eye balls
 * @param frame brush of the QR code eye frames
 */
@Stable
public class QrColors(
    public val dark : QrBrush = QrBrush.Default,
    public val light : QrBrush = QrBrush.Unspecified,
    public val ball : QrBrush = QrBrush.Unspecified,
    public val frame : QrBrush = QrBrush.Unspecified,
) {

    public fun copy(
        dark : QrBrush = this.dark,
        light : QrBrush = this.light,
        ball : QrBrush = this.ball,
        frame : QrBrush = this.frame
    ): QrColors = QrColors(dark, light, ball, frame)

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null || this::class != other::class) return false

        other as QrColors

        if (dark != other.dark) return false
        if (light != other.light) return false
        if (ball != other.ball) return false
        if (frame != other.frame) return false

        return true
    }

    override fun hashCode(): Int {
        var result = dark.hashCode()
        result = 31 * result + light.hashCode()
        result = 31 * result + ball.hashCode()
        result = 31 * result + frame.hashCode()
        return result
    }
}