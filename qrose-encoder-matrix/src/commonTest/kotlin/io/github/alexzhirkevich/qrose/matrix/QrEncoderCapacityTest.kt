package io.github.alexzhirkevich.qrose.matrix

import io.github.alexzhirkevich.qrose.QroseEncoders
import io.github.alexzhirkevich.qrose.matrix.qr.QrErrorCorrection
import kotlin.test.Test
import kotlin.test.assertEquals

class QrEncoderCapacityTest {

    @Test
    fun selectsSmallestVersionWhenPayloadExceedsSearchHint() {
        val matrix = QroseEncoders.QR(QrErrorCorrection.L).encode("a".repeat(1004))

        // 1004 byte-mode bytes fit in version 23-L, whose matrix is 109 modules wide.
        assertEquals(109, matrix.width)
        assertEquals(109, matrix.height)
    }
}
