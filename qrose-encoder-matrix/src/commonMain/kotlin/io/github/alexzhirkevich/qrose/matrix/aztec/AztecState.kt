package io.github.alexzhirkevich.qrose.matrix.aztec

internal class AztecState(
    val mode: Int,
    val token: AztecToken,
    val binaryShiftByteCount: Int,
    val bitCount: Int
) {
    companion object {
        val INITIAL_STATE = AztecState(
            mode = AztecHighLevelEncoder.MODE_UPPER,
            token = AztecToken.EMPTY,
            binaryShiftByteCount = 0,
            bitCount = 0
        )
    }

    fun latchAndAppend(mode: Int, value: Int): AztecState {
        var bitCount = this.bitCount
        var token = this.token
        if (mode != this.mode) {
            val latch = AztecHighLevelEncoder.LATCH_TABLE[this.mode][mode]
            token = token.add(latch and 0xFFFF, latch shr 16)
            bitCount += latch shr 16
        }
        val latchModeBitCount = if (mode == AztecHighLevelEncoder.MODE_DIGIT) 4 else 5
        token = token.add(value, latchModeBitCount)
        return AztecState(mode, token, 0, bitCount + latchModeBitCount)
    }

    fun shiftAndAppend(mode: Int, value: Int): AztecState {
        val token = this.token
        val thisModeBitCount = if (this.mode == AztecHighLevelEncoder.MODE_DIGIT) 4 else 5
        val shiftToken = token.add(AztecHighLevelEncoder.SHIFT_TABLE[this.mode][mode], thisModeBitCount)
        val appendedToken = shiftToken.add(value, 5)
        return AztecState(this.mode, appendedToken, 0, this.bitCount + thisModeBitCount + 5)
    }

    fun addBinaryShiftChar(index: Int): AztecState {
        var token = this.token
        var mode = this.mode
        var bitCount = this.bitCount
        if (this.mode == AztecHighLevelEncoder.MODE_PUNCT || this.mode == AztecHighLevelEncoder.MODE_DIGIT) {
            val latch = AztecHighLevelEncoder.LATCH_TABLE[this.mode][AztecHighLevelEncoder.MODE_UPPER]
            token = token.add(latch and 0xFFFF, latch shr 16)
            bitCount += latch shr 16
            mode = AztecHighLevelEncoder.MODE_UPPER
        }
        val deltaBitCount = if (binaryShiftByteCount == 0 || binaryShiftByteCount == 31) 18 else if (binaryShiftByteCount == 62) 9 else 8
        var result = AztecState(mode, token, binaryShiftByteCount + 1, bitCount + deltaBitCount)
        if (result.binaryShiftByteCount == 2047 + 31) {
            result = result.endBinaryShift(index + 1)
        }
        return result
    }

    fun endBinaryShift(index: Int): AztecState {
        if (binaryShiftByteCount == 0) return this
        val token = this.token.addBinaryShift(index - binaryShiftByteCount, binaryShiftByteCount)
        return AztecState(mode, token, 0, bitCount)
    }

    fun isBetterThanOrEqualTo(other: AztecState): Boolean {
        var mySize = this.bitCount + (AztecHighLevelEncoder.LATCH_TABLE[this.mode][other.mode] shr 16)
        if (this.binaryShiftByteCount < other.binaryShiftByteCount) {
            mySize += other.binaryShiftCost(other.binaryShiftByteCount) - this.binaryShiftCost(this.binaryShiftByteCount)
        } else if (this.binaryShiftByteCount > other.binaryShiftByteCount && other.binaryShiftByteCount > 0) {
            mySize += 10
        }
        return mySize <= other.bitCount
    }

    private fun binaryShiftCost(binaryShiftByteCount: Int): Int {
        if (binaryShiftByteCount > 62) return 21
        if (binaryShiftByteCount > 31) return 20
        if (binaryShiftByteCount > 0) return 10
        return 0
    }

    fun toBooleanArrayList(text: ByteArray): BooleanArrayList {
        val symbols = mutableListOf<AztecToken>()
        var current: AztecToken? = endBinaryShift(text.size).token
        while (current != null) {
            symbols.add(current)
            current = current.previous
        }
        val result = BooleanArrayList()
        for (i in symbols.size - 1 downTo 0) {
            symbols[i].appendTo(result, text)
        }
        return result
    }
}
