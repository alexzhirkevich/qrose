package io.github.alexzhirkevich.qrose.matrix.aztec

internal abstract class AztecToken(val previous: AztecToken?) {

    abstract fun appendTo(bitArray: BooleanArrayList, text: ByteArray)

    fun add(value: Int, bitCount: Int): AztecToken = SimpleToken(this, value, bitCount)

    fun addBinaryShift(start: Int, byteCount: Int): AztecToken = BinaryShiftToken(this, start, byteCount)

    companion object {
        val EMPTY: AztecToken = SimpleToken(null, 0, 0)
    }
}

internal class SimpleToken(
    previous: AztecToken?,
    private val value: Int,
    private val bitCount: Int
) : AztecToken(previous) {

    override fun appendTo(bitArray: BooleanArrayList, text: ByteArray) {
        bitArray.appendBits(value, bitCount)
    }

    override fun toString(): String {
        val v = (value and ((1 shl bitCount) - 1)) or (1 shl bitCount)
        return "<" + v.toString(2).substring(1) + '>'
    }
}

internal class BinaryShiftToken(
    previous: AztecToken?,
    private val binaryShiftStart: Int,
    private val binaryShiftByteCount: Int
) : AztecToken(previous) {

    override fun appendTo(bitArray: BooleanArrayList, text: ByteArray) {
        for (i in 0 until binaryShiftByteCount) {
            if (i == 0 || (i == 31 && binaryShiftByteCount <= 62)) {
                bitArray.appendBits(31, 5) // BINARY_SHIFT
                if (binaryShiftByteCount > 62) {
                    bitArray.appendBits(binaryShiftByteCount - 31, 16)
                } else if (i == 0) {
                    bitArray.appendBits(minOf(binaryShiftByteCount, 31), 5)
                } else {
                    bitArray.appendBits(binaryShiftByteCount - 31, 5)
                }
            }
            bitArray.appendBits(text[binaryShiftStart + i].toInt() and 0xFF, 8)
        }
    }
}

internal class BooleanArrayList {
    private var data = BooleanArray(64)
    var size = 0
        private set

    fun appendBit(bit: Boolean) {
        ensureCapacity(size + 1)
        data[size++] = bit
    }

    fun appendBits(value: Int, bitCount: Int) {
        for (i in bitCount - 1 downTo 0) {
            appendBit(((value shr i) and 1) != 0)
        }
    }

    operator fun get(index: Int): Boolean {
        require(index in 0 until size)
        return data[index]
    }

    operator fun set(index: Int, value: Boolean) {
        require(index in 0 until size)
        data[index] = value
    }

    fun toBooleanArray(): BooleanArray = data.copyOf(size)

    private fun ensureCapacity(minCapacity: Int) {
        if (minCapacity > data.size) {
            var newCap = data.size * 2
            if (newCap < minCapacity) newCap = minCapacity
            data = data.copyOf(newCap)
        }
    }
}
