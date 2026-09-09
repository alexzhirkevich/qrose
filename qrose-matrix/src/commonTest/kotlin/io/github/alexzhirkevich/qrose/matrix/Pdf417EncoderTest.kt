package io.github.alexzhirkevich.qrose.matrix

import io.github.alexzhirkevich.qrose.matrix.pdf417.Pdf417Codewords
import io.github.alexzhirkevich.qrose.matrix.pdf417.Pdf417Compaction
import io.github.alexzhirkevich.qrose.matrix.pdf417.Pdf417Dimensions
import io.github.alexzhirkevich.qrose.matrix.pdf417.Pdf417Encoder
import io.github.alexzhirkevich.qrose.matrix.pdf417.Pdf417ErrorCorrection
import io.github.alexzhirkevich.qrose.matrix.pdf417.Pdf417ErrorCorrectionLevel
import kotlin.test.Test
import kotlin.test.assertContentEquals
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class Pdf417EncoderTest {

    @Test
    fun testBasicPdf417() {
        val matrix = Pdf417Encoder.encode("Hello, PDF417!")
        assertTrue(matrix.width > 0)
        assertTrue(matrix.height >= 3)

        // Verify start pattern: 0x1fea8 (17 bits)
        val startPattern = Pdf417Codewords.START_PATTERN
        for (y in 0 until matrix.height) {
            for (bit in 16 downTo 0) {
                val expected = ((startPattern shr bit) and 1) != 0
                val col = 16 - bit
                assertEquals(expected, matrix[col, y], "Start pattern mismatch at bit $bit, row $y")
            }
        }

        // Verify stop pattern: 0x3fa29 (18 bits)
        val stopPattern = Pdf417Codewords.STOP_PATTERN
        for (y in 0 until matrix.height) {
            for (bit in 17 downTo 0) {
                val expected = ((stopPattern shr bit) and 1) != 0
                val col = matrix.width - 1 - bit
                assertEquals(expected, matrix[col, y], "Stop pattern mismatch at bit $bit, row $y")
            }
        }
    }

    @Test
    fun testCompactPdf417() {
        val standard = Pdf417Encoder.encode("Compact Test", compact = false)
        val compact = Pdf417Encoder.encode("Compact Test", compact = true)

        // Compact symbol omits right row indicator (17 modules) and uses 1-module stop bar instead of 18 (17 modules saved)
        // Total saving is 34 modules
        assertEquals(standard.width - 34, compact.width)
        assertEquals(standard.height, compact.height)

        // Stop bar in compact PDF417 is a single dark module at the end of each row
        for (y in 0 until compact.height) {
            assertTrue(compact[compact.width - 1, y], "Compact stop bar at row $y must be dark")
        }
    }

    @Test
    fun testCompactionModes() {
        // Text mode
        val textMatrix = Pdf417Encoder.encode(
            data = "UPPER lower 12345!.,-",
            compaction = Pdf417Compaction.Text
        )
        assertTrue(textMatrix.width > 0)

        // Numeric mode
        val numMatrix = Pdf417Encoder.encode(
            data = "012345678901234567890123456789",
            compaction = Pdf417Compaction.Numeric
        )
        assertTrue(numMatrix.width > 0)

        // Byte mode with binary data
        val byteMatrix = Pdf417Encoder.encode(
            data = "Binary\u0000\u0001\u0002Payload",
            compaction = Pdf417Compaction.Byte
        )
        assertTrue(byteMatrix.width > 0)

        // Auto mode with UTF-8 non-ASCII characters
        val utf8Matrix = Pdf417Encoder.encode(
            data = "Привет, мир! PDF417 2D barcode"
        )
        assertTrue(utf8Matrix.width > 0)
    }

    @Test
    fun testCustomDimensions() {
        val matrix = Pdf417Encoder.encode(
            data = "Fixed 4 columns test",
            dimensions = Pdf417Dimensions(minCols = 4, maxCols = 4, minRows = 3, maxRows = 20)
        )
        // 17 * 4 data columns + 69 overhead = 137 modules
        assertEquals(17 * 4 + 69, matrix.width)
    }

    @Test
    fun testErrorCorrectionLevels() {
        val m0 = Pdf417Encoder.encode("EC Test", errorCorrectionLevel = Pdf417ErrorCorrectionLevel.Level0)
        val m4 = Pdf417Encoder.encode("EC Test", errorCorrectionLevel = Pdf417ErrorCorrectionLevel.Level4)

        // Level 4 has 32 EC codewords vs 2 EC codewords in Level 0, requiring more total cells
        assertTrue(m4.width * m4.height > m0.width * m0.height)
    }

    @Test
    fun testReedSolomonKnownCoefficients() {
        val data = intArrayOf(5, 453, 178, 900, 900)
        val ec0 = Pdf417ErrorCorrection.generateErrorCorrection(data, 0)
        assertContentEquals(intArrayOf(799, 565), ec0)

        val ec1 = Pdf417ErrorCorrection.generateErrorCorrection(data, 1)
        assertContentEquals(intArrayOf(415, 424, 170, 908), ec1)
    }

    @Test
    fun testPdf417Painter() {
        val painter = Pdf417Painter(
            data = "QRose PDF417 Painter",
            quietZone = 2,
            rowHeightRatio = 3.0f
        )
        assertTrue(painter.intrinsicSize.width > 0f)
        assertTrue(painter.intrinsicSize.height > 0f)
        assertEquals(3.0f, painter.rowHeightRatio)
    }
}
