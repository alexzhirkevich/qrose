package io.github.alexzhirkevich.qrose.matrix.datamatrix

internal class SymbolInfo(
    val rectangular: Boolean,
    val dataCapacity: Int,
    val errorCodewords: Int,
    val matrixWidth: Int,
    val matrixHeight: Int,
    val dataRegionWidth: Int,
    val dataRegionHeight: Int,
    val rsBlocks: Int = 1,
) {
    val symbolDataWidth: Int
        get() = (matrixWidth / (dataRegionWidth + 2)) * dataRegionWidth

    val symbolDataHeight: Int
        get() = (matrixHeight / (dataRegionHeight + 2)) * dataRegionHeight

    val horizontalDataRegions: Int
        get() = matrixWidth / (dataRegionWidth + 2)

    val verticalDataRegions: Int
        get() = matrixHeight / (dataRegionHeight + 2)

    val totalCodewords: Int
        get() = dataCapacity + errorCodewords

    fun getDataLengthForInterleavedBlock(index: Int): Int {
        val base = dataCapacity / rsBlocks
        val remainder = dataCapacity % rsBlocks
        return if (index <= remainder) base + 1 else base
    }

    fun getErrorLengthForInterleavedBlock(): Int {
        return errorCodewords / rsBlocks
    }

    companion object {
        private val SYMBOLS = arrayOf(
            // 24 Square symbols
            SymbolInfo(false, 3, 5, 10, 10, 8, 8),
            SymbolInfo(false, 5, 7, 12, 12, 10, 10),
            SymbolInfo(false, 8, 10, 14, 14, 12, 12),
            SymbolInfo(false, 12, 12, 16, 16, 14, 14),
            SymbolInfo(false, 18, 14, 18, 18, 16, 16),
            SymbolInfo(false, 22, 18, 20, 20, 18, 18),
            SymbolInfo(false, 30, 20, 22, 22, 20, 20),
            SymbolInfo(false, 36, 24, 24, 24, 22, 22),
            SymbolInfo(false, 44, 28, 26, 26, 24, 24),
            SymbolInfo(false, 62, 36, 32, 32, 14, 14),
            SymbolInfo(false, 86, 42, 36, 36, 16, 16),
            SymbolInfo(false, 114, 48, 40, 40, 18, 18),
            SymbolInfo(false, 144, 56, 44, 44, 20, 20),
            SymbolInfo(false, 174, 68, 48, 48, 22, 22),
            SymbolInfo(false, 204, 84, 52, 52, 24, 24, 2),
            SymbolInfo(false, 280, 112, 64, 64, 14, 14, 2),
            SymbolInfo(false, 368, 144, 72, 72, 16, 16, 4),
            SymbolInfo(false, 456, 192, 80, 80, 18, 18, 4),
            SymbolInfo(false, 576, 224, 88, 88, 20, 20, 4),
            SymbolInfo(false, 696, 272, 96, 96, 22, 22, 4),
            SymbolInfo(false, 816, 336, 104, 104, 24, 24, 6),
            SymbolInfo(false, 1050, 408, 120, 120, 18, 18, 6),
            SymbolInfo(false, 1304, 496, 132, 132, 20, 20, 8),
            SymbolInfo(false, 1558, 620, 144, 144, 22, 22, 10),

            // 6 Rectangular symbols
            SymbolInfo(true, 5, 7, 18, 8, 16, 6),
            SymbolInfo(true, 10, 11, 32, 8, 14, 6),
            SymbolInfo(true, 16, 14, 26, 12, 24, 10),
            SymbolInfo(true, 22, 18, 36, 12, 16, 10),
            SymbolInfo(true, 32, 24, 36, 16, 16, 12),
            SymbolInfo(true, 49, 28, 48, 16, 22, 12)
        )

        fun lookup(dataCodewords: Int, shape: DataMatrixShape): SymbolInfo {
            for (symbol in SYMBOLS) {
                if (shape == DataMatrixShape.Square && symbol.rectangular) continue
                if (shape == DataMatrixShape.Rectangle && !symbol.rectangular) continue
                if (dataCodewords <= symbol.dataCapacity) {
                    return symbol
                }
            }
            throw IllegalArgumentException(
                "Can't find a symbol arrangement that matches the query. Data codewords: $dataCodewords, shape: $shape"
            )
        }
    }
}
