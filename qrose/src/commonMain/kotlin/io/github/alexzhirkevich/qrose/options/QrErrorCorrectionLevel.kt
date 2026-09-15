@file:Suppress("UNUSED")

package io.github.alexzhirkevich.qrose.options

import androidx.compose.runtime.Immutable
import io.github.alexzhirkevich.qrose.matrix.qr.QrErrorCorrection


/**
 * QR code allows you to read encoded information even if a
 * part of the QR code image is damaged. It also allows to have logo
 * inside the code as a part of "damage".
 * */
@Immutable
public enum class QrErrorCorrectionLevel(
    internal val lvl : QrErrorCorrection
) {

    /**
     * Minimum possible level will be used.
     * */
    Auto(QrErrorCorrection.L),

    /**
     * ~7% of QR code can be damaged (or used as logo).
     * */
    Low(QrErrorCorrection.L),

    /**
     * ~15% of QR code can be damaged (or used as logo).
     * */
    Medium(QrErrorCorrection.M),

    /**
     * ~25% of QR code can be damaged (or used as logo).
     * */
    MediumHigh(QrErrorCorrection.Q),

    /**
     * ~30% of QR code can be damaged (or used as logo).
     * */
    High(QrErrorCorrection.H)
}
