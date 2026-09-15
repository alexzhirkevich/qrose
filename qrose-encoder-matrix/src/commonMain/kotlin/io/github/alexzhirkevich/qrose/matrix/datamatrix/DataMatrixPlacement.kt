package io.github.alexzhirkevich.qrose.matrix.datamatrix

internal class DataMatrixPlacement(
    private val codewords: IntArray,
    val numcols: Int,
    val numrows: Int
) {
    // 0 = unassigned, 1 = true (dark), 2 = false (light)
    private val array = ByteArray(numrows * numcols)

    fun getBit(col: Int, row: Int): Boolean = array[row * numcols + col].toInt() == 1

    private fun setBit(col: Int, row: Int, bit: Boolean) {
        array[row * numcols + col] = (if (bit) 1 else 2).toByte()
    }

    private fun hasBit(col: Int, row: Int): Boolean = array[row * numcols + col].toInt() > 0

    fun place() {
        var pos = 0
        var row = 4
        var col = 0

        do {
            // Check corner 1
            if (row == numrows && col == 0) {
                corner1(pos++)
            }
            // Check corner 2
            if (row == numrows - 2 && col == 0 && numcols % 4 != 0) {
                corner2(pos++)
            }
            // Check corner 3
            if (row == numrows - 2 && col == 0 && numcols % 8 == 4) {
                corner3(pos++)
            }
            // Check corner 4
            if (row == numrows + 4 && col == 2 && numcols % 8 == 0) {
                corner4(pos++)
            }

            // Up-right scan
            do {
                if (row < numrows && col >= 0 && !hasBit(col, row)) {
                    utah(row, col, pos++)
                }
                row -= 2
                col += 2
            } while (row >= 0 && col < numcols)
            row += 1
            col += 3

            // Down-left scan
            do {
                if (row >= 0 && col < numcols && !hasBit(col, row)) {
                    utah(row, col, pos++)
                }
                row += 2
                col -= 2
            } while (row < numrows && col >= 0)
            row += 3
            col += 1
        } while (row < numrows || col < numcols)

        // Point in lower right may not be set
        if (!hasBit(numcols - 1, numrows - 1)) {
            setBit(numcols - 1, numrows - 1, true)
            setBit(numcols - 2, numrows - 2, true)
        }
    }

    private fun module(row: Int, col: Int, pos: Int, bit: Int) {
        var r = row
        var c = col
        if (r < 0) {
            r += numrows
            c += 4 - ((numrows + 4) % 8)
        }
        if (c < 0) {
            c += numcols
            r += 4 - ((numcols + 4) % 8)
        }
        val v = codewords[pos]
        val isDark = (v and (1 shl (8 - bit))) != 0
        setBit(c, r, isDark)
    }

    private fun utah(row: Int, col: Int, pos: Int) {
        module(row - 2, col - 2, pos, 1)
        module(row - 2, col - 1, pos, 2)
        module(row - 1, col - 2, pos, 3)
        module(row - 1, col - 1, pos, 4)
        module(row - 1, col, pos, 5)
        module(row, col - 2, pos, 6)
        module(row, col - 1, pos, 7)
        module(row, col, pos, 8)
    }

    private fun corner1(pos: Int) {
        module(numrows - 1, 0, pos, 1)
        module(numrows - 1, 1, pos, 2)
        module(numrows - 1, 2, pos, 3)
        module(0, numcols - 2, pos, 4)
        module(0, numcols - 1, pos, 5)
        module(1, numcols - 1, pos, 6)
        module(2, numcols - 1, pos, 7)
        module(3, numcols - 1, pos, 8)
    }

    private fun corner2(pos: Int) {
        module(numrows - 3, 0, pos, 1)
        module(numrows - 2, 0, pos, 2)
        module(numrows - 1, 0, pos, 3)
        module(0, numcols - 4, pos, 4)
        module(0, numcols - 3, pos, 5)
        module(0, numcols - 2, pos, 6)
        module(0, numcols - 1, pos, 7)
        module(1, numcols - 1, pos, 8)
    }

    private fun corner3(pos: Int) {
        module(numrows - 3, 0, pos, 1)
        module(numrows - 2, 0, pos, 2)
        module(numrows - 1, 0, pos, 3)
        module(0, numcols - 2, pos, 4)
        module(0, numcols - 1, pos, 5)
        module(1, numcols - 1, pos, 6)
        module(2, numcols - 1, pos, 7)
        module(3, numcols - 1, pos, 8)
    }

    private fun corner4(pos: Int) {
        module(numrows - 1, 0, pos, 1)
        module(numrows - 1, numcols - 1, pos, 2)
        module(0, numcols - 3, pos, 3)
        module(0, numcols - 2, pos, 4)
        module(0, numcols - 1, pos, 5)
        module(1, numcols - 3, pos, 6)
        module(1, numcols - 2, pos, 7)
        module(1, numcols - 1, pos, 8)
    }
}
