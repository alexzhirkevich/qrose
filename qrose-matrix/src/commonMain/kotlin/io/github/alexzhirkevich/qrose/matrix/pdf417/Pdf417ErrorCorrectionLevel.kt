package io.github.alexzhirkevich.qrose.matrix.pdf417

/**
 * PDF417 Error Correction Level (Levels 0 through 8).
 *
 * Higher levels provide stronger error correction at the expense of barcode size.
 */
enum class Pdf417ErrorCorrectionLevel(val level: Int) {
    /**
     * Automatically select recommended error correction level based on data length:
     * - &lt;= 40 codewords: Level 2
     * - &lt;= 160 codewords: Level 3
     * - &lt;= 320 codewords: Level 4
     * - &lt;= 863 codewords: Level 5
     */
    Auto(-1),

    /** Level 0: 2 error correction codewords */
    Level0(0),

    /** Level 1: 4 error correction codewords */
    Level1(1),

    /** Level 2: 8 error correction codewords */
    Level2(2),

    /** Level 3: 16 error correction codewords */
    Level3(3),

    /** Level 4: 32 error correction codewords */
    Level4(4),

    /** Level 5: 64 error correction codewords */
    Level5(5),

    /** Level 6: 128 error correction codewords */
    Level6(6),

    /** Level 7: 256 error correction codewords */
    Level7(7),

    /** Level 8: 512 error correction codewords */
    Level8(8);
}
