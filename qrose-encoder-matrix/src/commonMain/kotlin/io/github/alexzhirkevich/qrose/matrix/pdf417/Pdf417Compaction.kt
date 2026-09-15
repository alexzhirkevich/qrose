package io.github.alexzhirkevich.qrose.matrix.pdf417

/**
 * Data compaction mode for PDF417.
 */
enum class Pdf417Compaction {
    /**
     * Automatically chooses the best compaction mode (Text, Numeric, Byte)
     * based on the input data according to ISO/IEC 15438.
     */
    Auto,

    /**
     * Enforces Text Compaction mode (encodes uppercase, lowercase, numbers, punctuation).
     */
    Text,

    /**
     * Enforces Byte Compaction mode (encodes arbitrary 8-bit bytes, 6 bytes per 5 codewords).
     */
    Byte,

    /**
     * Enforces Numeric Compaction mode (compacts digits, up to 44 digits per block).
     */
    Numeric
}
