package io.github.alexzhirkevich.qrose.matrix

import io.github.alexzhirkevich.qrose.matrix.aztec.AztecEncoder
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class AztecEncoderTest {

    @Test
    fun testCompactAztecCode() {
        val matrix = AztecEncoder.encode("Aztec")
        assertEquals(matrix.width, matrix.height)

        val center = matrix.width / 2

        // Center must be dark
        assertTrue(matrix[center, center], "Center module must be dark")

        // Ring 1 (distance 1 from center) must be light (except possible corner orientation bits)
        assertFalse(matrix[center - 1, center], "Ring 1 top must be light")
        assertFalse(matrix[center + 1, center], "Ring 1 bottom must be light")
        assertFalse(matrix[center, center - 1], "Ring 1 left must be light")
        assertFalse(matrix[center, center + 1], "Ring 1 right must be light")

        // Ring 2 (distance 2 from center) must be dark
        assertTrue(matrix[center - 2, center], "Ring 2 top must be dark")
        assertTrue(matrix[center + 2, center], "Ring 2 bottom must be dark")
        assertTrue(matrix[center, center - 2], "Ring 2 left must be dark")
        assertTrue(matrix[center, center + 2], "Ring 2 right must be dark")
    }

    @Test
    fun testLongTextAztecCode() {
        val text = "Aztec Code 2D matrix barcode specification ISO/IEC 24778 testing long string data payload."
        val matrix = AztecEncoder.encode(text)
        assertEquals(matrix.width, matrix.height)
        assertTrue(matrix.width >= 23)

        val center = matrix.width / 2
        assertTrue(matrix[center, center], "Bullseye center must be dark")
    }
}
