package io.github.alexzhirkevich.qrose.matrix

import io.github.alexzhirkevich.qrose.QroseEncoders
import io.github.alexzhirkevich.qrose.matrix.qr.QrErrorCorrection
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFails
import kotlin.test.assertFailsWith
import kotlin.test.assertTrue

class QrVersionTest {

    // Max byte-mode capacity of version 40 per ECL (ISO/IEC 18004)
    private val v40ByteCapacity = mapOf(
        QrErrorCorrection.L to 2953,
        QrErrorCorrection.M to 2331,
        QrErrorCorrection.Q to 1663,
        QrErrorCorrection.H to 1273,
    )


    private val levels = listOf(
        QrErrorCorrection.L,
        QrErrorCorrection.M,
        QrErrorCorrection.Q,
        QrErrorCorrection.H,
    )

    // Byte-mode capacity per version (index 0 = version 1). Columns: L, M, Q, H.
    // Source: ISO/IEC 18004 capacity table.
    private val byteCapacity: List<IntArray> = listOf(
        intArrayOf(17, 14, 11, 7),
        intArrayOf(32, 26, 20, 14),
        intArrayOf(53, 42, 32, 24),
        intArrayOf(78, 62, 46, 34),
        intArrayOf(106, 84, 60, 44),
        intArrayOf(134, 106, 74, 58),
        intArrayOf(154, 122, 86, 64),
        intArrayOf(192, 152, 108, 84),
        intArrayOf(230, 180, 130, 98),
        intArrayOf(271, 213, 151, 119),
        intArrayOf(321, 251, 177, 137),
        intArrayOf(367, 287, 203, 155),
        intArrayOf(425, 331, 241, 177),
        intArrayOf(458, 362, 258, 194),
        intArrayOf(520, 412, 292, 220),
        intArrayOf(586, 450, 322, 250),
        intArrayOf(644, 504, 364, 280),
        intArrayOf(718, 560, 394, 310),
        intArrayOf(792, 624, 442, 338),
        intArrayOf(858, 666, 482, 382),
        intArrayOf(929, 711, 509, 403),
        intArrayOf(1003, 779, 565, 439),
        intArrayOf(1091, 857, 611, 461),
        intArrayOf(1171, 911, 661, 511),
        intArrayOf(1273, 997, 715, 535),
        intArrayOf(1367, 1059, 751, 593),
        intArrayOf(1465, 1125, 805, 625),
        intArrayOf(1528, 1190, 868, 658),
        intArrayOf(1628, 1264, 908, 698),
        intArrayOf(1732, 1370, 982, 742),
        intArrayOf(1840, 1452, 1030, 790),
        intArrayOf(1952, 1538, 1112, 842),
        intArrayOf(2068, 1628, 1168, 898),
        intArrayOf(2188, 1722, 1228, 958),
        intArrayOf(2303, 1809, 1283, 983),
        intArrayOf(2431, 1911, 1351, 1051),
        intArrayOf(2563, 1989, 1423, 1093),
        intArrayOf(2699, 2099, 1499, 1139),
        intArrayOf(2809, 2213, 1579, 1219),
        intArrayOf(2953, 2331, 1663, 1273),
    )

    private fun capacity(version: Int, ecl: Int) = byteCapacity[version - 1][ecl]

    // Lowercase ASCII has no numeric/alphanumeric representation, so the encoder
    // must use byte mode, and length in chars == length in bytes.
    private fun payload(bytes: Int) = "a".repeat(bytes)

    private fun sizeOf(version: Int) = 17 + 4 * version


    @Test
    fun payloadAtVersion40CapacityFits() {
        v40ByteCapacity.forEach { (ecl, max) ->
            val matrix = QroseEncoders.QR(ecl).encode("a".repeat(max))
            assertEquals(177, matrix.width, "ECL $ecl, $max bytes should be exactly v40")
            assertEquals(177, matrix.height)
        }
    }

    @Test
    fun payloadOneByteOverVersion40CapacityFails() {
        v40ByteCapacity.forEach { (ecl, max) ->
            assertFailsWith<IllegalArgumentException> {
                QroseEncoders.QR(ecl).encode("a".repeat(max + 1))
            }
        }
    }

    @Test
    fun hugePayloadFailsForEveryLevel() {
        // 7090 chars exceeds even the largest possible capacity of any QR mode
        // (v40-L numeric = 7089), so it can't fit regardless of how the encoder picks the mode.
        val payload = "1".repeat(7090)
        QrErrorCorrection.values().forEach { ecl ->
            assertFails("ECL $ecl must reject 7090 digits") {
                QroseEncoders.QR(ecl).encode(payload)
            }
        }
    }

    /** Guards against typos in the table above. */
    @Test
    fun capacityTableIsSane() {
        assertEquals(40, byteCapacity.size)
        for (ecl in levels.indices) {
            for (v in 2..40) {
                assertTrue(
                    capacity(v, ecl) > capacity(v - 1, ecl),
                    "capacity must grow with version (v=$v, ecl=${levels[ecl]})"
                )
            }
        }
        for (v in 1..40) {
            for (ecl in 1..3) {
                assertTrue(
                    capacity(v, ecl - 1) > capacity(v, ecl),
                    "capacity must drop as ECL rises (v=$v, ecl=${levels[ecl]})"
                )
            }
        }
    }

    /** A payload of exactly capacity(v) bytes must produce version v. */
    @Test
    fun exactCapacitySelectsThatVersion() {
        val failures = mutableListOf<String>()
        for ((eclIdx, ecl) in levels.withIndex()) {
            val encoder = QroseEncoders.QR(ecl)
            for (v in 1..40) {
                val n = capacity(v, eclIdx)
                val actual = runCatching { encoder.encode(payload(n)).width }
                if (actual.getOrNull() != sizeOf(v)) {
                    failures += "ECL=$ecl v=$v bytes=$n expected=${sizeOf(v)} actual=$actual"
                }
            }
        }
        assertTrue(failures.isEmpty(), failures.joinToString("\n"))
    }

    /** One byte over capacity(v) must move up to version v + 1. */
    @Test
    fun oneByteOverCapacitySelectsNextVersion() {
        val failures = mutableListOf<String>()
        for ((eclIdx, ecl) in levels.withIndex()) {
            val encoder = QroseEncoders.QR(ecl)
            for (v in 1..39) {
                val n = capacity(v, eclIdx) + 1
                val actual = runCatching { encoder.encode(payload(n)).width }
                if (actual.getOrNull() != sizeOf(v + 1)) {
                    failures += "ECL=$ecl bytes=$n expected v${v + 1}=${sizeOf(v + 1)} actual=$actual"
                }
            }
        }
        assertTrue(failures.isEmpty(), failures.joinToString("\n"))
    }

    /** Anything beyond version 40 must be rejected, not silently encoded. */
    @Test
    fun oneByteOverVersion40Fails() {
        for ((eclIdx, ecl) in levels.withIndex()) {
            val n = capacity(40, eclIdx) + 1
            assertFails("ECL=$ecl, $n bytes must not encode") {
                QroseEncoders.QR(ecl).encode(payload(n))
            }
        }
    }
}