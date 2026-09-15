package io.github.alexzhirkevich.qrose.matrix.qr.internals

import io.github.alexzhirkevich.qrose.matrix.qr.QrErrorCorrection
import io.github.alexzhirkevich.qrose.matrix.qr.QrMaskPattern

/**
 * Object with helper methods and constants to setup stuff into the QRCode such as Position Probes and Timing Probes.
 *
 * @author Rafael Lins - g0dkar
 */
internal object QRCodeSetup {
    private const val DEFAULT_PROBE_SIZE = 7

    fun setupTopLeftPositionProbePattern(
        modules: Array<Array<Boolean?>>,
        probeSize: Int = DEFAULT_PROBE_SIZE
    ) {
        setupPositionProbePattern(0, 0, modules, probeSize)
    }

    fun setupTopRightPositionProbePattern(
        modules: Array<Array<Boolean?>>,
        probeSize: Int = DEFAULT_PROBE_SIZE
    ) {
        setupPositionProbePattern(modules.size - probeSize, 0, modules, probeSize)
    }

    fun setupBottomLeftPositionProbePattern(
        modules: Array<Array<Boolean?>>,
        probeSize: Int = DEFAULT_PROBE_SIZE
    ) {
        setupPositionProbePattern(0, modules.size - probeSize, modules, probeSize)
    }

    fun setupPositionProbePattern(
        rowOffset: Int,
        colOffset: Int,
        modules: Array<Array<Boolean?>>,
        probeSize: Int = DEFAULT_PROBE_SIZE
    ) {
        val modulesSize = modules.size

        for (row in -1..probeSize) {
            for (col in -1..probeSize) {
                if (!isInsideModules(row, rowOffset, col, colOffset, modulesSize)) {
                    continue
                }

                val isDark = isTopBottomRowSquare(row, col, probeSize) ||
                    isLeftRightColSquare(row, col, probeSize) ||
                    isMidSquare(row, col, probeSize)

                modules[row + rowOffset][col + colOffset] = isDark
            }
        }
    }

    private fun isInsideModules(row: Int, rowOffset: Int, col: Int, colOffset: Int, modulesSize: Int) =
        row + rowOffset in 0 until modulesSize && col + colOffset in 0 until modulesSize

    private fun isTopBottomRowSquare(row: Int, col: Int, probeSize: Int) =
        col in 0 until probeSize && (row == 0 || row == probeSize - 1)

    private fun isLeftRightColSquare(row: Int, col: Int, probeSize: Int) =
        row in 0 until probeSize && (col == 0 || col == probeSize - 1)

    private fun isMidSquare(row: Int, col: Int, probeSize: Int) =
        row in 2 until (probeSize - 2) && 2 <= col && col <= probeSize - 3

    fun setupPositionAdjustPattern(type: Int, modules: Array<Array<Boolean?>>) {
        val pos = QRUtil.getPatternPosition(type)

        for (i in pos.indices) {
            for (j in pos.indices) {
                val row = pos[i]
                val col = pos[j]

                if (modules[row][col] != null) {
                    continue
                }

                for (r in -2..2) {
                    for (c in -2..2) {
                        modules[row + r][col + c] =
                            r == -2 || r == 2 || c == -2 || c == 2 || r == 0 && c == 0
                    }
                }
            }
        }
    }

    fun setupTimingPattern(moduleCount: Int, modules: Array<Array<Boolean?>>) {
        for (r in 8 until moduleCount - 8) {
            if (modules[r][6] != null) {
                continue
            }

            modules[r][6] =  r % 2 == 0
        }

        for (c in 8 until moduleCount - 8) {
            if (modules[6][c] != null) {
                continue
            }

            modules[6][c] = c % 2 == 0
        }
    }

    fun setupTypeInfo(
        errorCorrectionLevel: QrErrorCorrection,
        maskPattern: QrMaskPattern,
        moduleCount: Int,
        modules: Array<Array<Boolean?>>
    ) {
        val data = errorCorrectionLevel.value shl 3 or maskPattern.ordinal
        val bits = QRUtil.getBCHTypeInfo(data)

        for (i in 0..14) {
            val mod = bits shr i and 1 == 1

            if (i < 6) {
                set(i, 8, mod, modules)
            } else if (i < 8) {
                set(i + 1, 8, mod, modules)
            } else {
                set(moduleCount - 15 + i, 8, mod, modules)
            }
        }

        for (i in 0..14) {
            val mod = bits shr i and 1 == 1

            if (i < 8) {
                set(8, moduleCount - i - 1, mod, modules)
            } else if (i < 9) {
                set(8, 15 - i, mod, modules)
            } else {
                set(8, 15 - i - 1, mod, modules)
            }
        }

        set(moduleCount - 8, 8, true, modules)
    }

    fun setupTypeNumber(type: Int, moduleCount: Int, modules: Array<Array<Boolean?>>) {
        val bits = QRUtil.getBCHTypeNumber(type)

        for (i in 0..17) {
            val mod = bits shr i and 1 == 1
            set(i / 3, i % 3 + moduleCount - 8 - 3, mod, modules)
        }

        for (i in 0..17) {
            val mod = bits shr i and 1 == 1
            set(i % 3 + moduleCount - 8 - 3, i / 3, mod, modules)
        }
    }

    fun applyMaskPattern(
        data: IntArray,
        maskPattern: QrMaskPattern,
        moduleCount: Int,
        modules: Array<Array<Boolean?>>
    ) {
        var inc = -1
        var bitIndex = 7
        var byteIndex = 0
        var row = moduleCount - 1
        var col = moduleCount - 1

        while (col > 0) {
            if (col == 6) {
                col--
            }

            while (true) {
                for (c in 0..1) {
                    if (modules[row][col - c] == null) {
                        var dark = false

                        if (byteIndex < data.size) {
                            dark = (data[byteIndex] ushr bitIndex) and 1 == 1
                        }

                        val mask = QRUtil.getMask(maskPattern, row, col - c)
                        if (mask) {
                            dark = !dark
                        }

                        set(row, col - c, dark, modules)

                        bitIndex--
                        if (bitIndex == -1) {
                            byteIndex++
                            bitIndex = 7
                        }
                    }
                }

                row += inc
                if (row !in 0..<moduleCount) {
                    row -= inc
                    inc = -inc
                    break
                }
            }

            col -= 2
        }
    }

    private fun set(row: Int, col: Int, value: Boolean, modules: Array<Array<Boolean?>>) {
        modules[row][col] = value
    }
}
