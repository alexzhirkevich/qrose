package io.github.alexzhirkevich.qrose.matrix

import io.github.alexzhirkevich.qrose.matrix.datamatrix.DataMatrixEncoder
import io.github.alexzhirkevich.qrose.matrix.datamatrix.DataMatrixShape
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class DataMatrixEncoderTest {

    @Test
    fun testSmallSquareDataMatrix() {
        val matrix = DataMatrixEncoder.encode("Test", DataMatrixShape.Square)
        assertEquals(matrix.width, matrix.height)
        assertTrue(matrix.width >= 10)

        // Left border must be solid dark
        for (y in 0 until matrix.height) {
            assertTrue(matrix[0, y], "Left border at y=$y must be dark")
        }

        // Bottom border must be solid dark
        for (x in 0 until matrix.width) {
            assertTrue(matrix[x, matrix.height - 1], "Bottom border at x=$x must be dark")
        }

        // Top border must alternate: x % 2 == 0 is dark
        for (x in 0 until matrix.width) {
            assertEquals(x % 2 == 0, matrix[x, 0], "Top clock track at x=$x")
        }

        // Right border must alternate: y % 2 == 1 is dark
        for (y in 0 until matrix.height) {
            assertEquals(y % 2 == 1, matrix[matrix.width - 1, y], "Right clock track at y=$y")
        }
    }

    @Test
    fun testRectangularDataMatrix() {
        val matrix = DataMatrixEncoder.encode("TEST1234", DataMatrixShape.Rectangle)
        assertTrue(matrix.width > matrix.height, "Rectangular matrix width (${matrix.width}) must exceed height (${matrix.height})")

        // Left border of first region is solid dark
        for (y in 0 until matrix.height) {
            assertTrue(matrix[0, y], "Left border at y=$y must be dark")
        }

        // Bottom border is solid dark
        for (x in 0 until matrix.width) {
            // Note: each region has bottom solid dark
            if (x < matrix.width) {
                // at bottom of matrix
                assertTrue(matrix[x, matrix.height - 1], "Bottom border at x=$x must be dark")
            }
        }
    }

    @Test
    fun testDigitsDataMatrix() {
        val matrix = DataMatrixEncoder.encode("01234567890123456789")
        assertTrue(matrix.width >= 14)
        assertTrue(matrix.height >= 14)
    }

    @Test
    fun testLongTextMultiRegionDataMatrix() {
        val text = "Compose Multiplatform Data Matrix encoding supporting ISO/IEC 16022 standard!"
        val matrix = DataMatrixEncoder.encode(text, DataMatrixShape.Square)
        assertTrue(matrix.width >= 32)
        assertEquals(matrix.width, matrix.height)
    }
}
