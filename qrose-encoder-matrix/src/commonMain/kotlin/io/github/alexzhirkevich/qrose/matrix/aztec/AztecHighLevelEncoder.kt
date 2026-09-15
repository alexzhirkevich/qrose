package io.github.alexzhirkevich.qrose.matrix.aztec

internal class AztecHighLevelEncoder(private val text: ByteArray) {

    fun encode(): BooleanArrayList {
        var states: Collection<AztecState> = listOf(AztecState.INITIAL_STATE)
        var index = 0
        while (index < text.size) {
            val nextChar = if (index + 1 < text.size) text[index + 1] else 0
            val pairCode = when (text[index].toInt()) {
                '\r'.code -> if (nextChar.toInt() == '\n'.code) 2 else 0
                '.'.code -> if (nextChar.toInt() == ' '.code) 3 else 0
                ','.code -> if (nextChar.toInt() == ' '.code) 4 else 0
                ':'.code -> if (nextChar.toInt() == ' '.code) 5 else 0
                else -> 0
            }

            states = if (pairCode > 0) {
                updateStateListForPair(states, index, pairCode)
            } else {
                updateStateListForChar(states, index)
            }
            index += if (pairCode > 0) 2 else 1
        }

        var minState: AztecState? = null
        for (state in states) {
            if (minState == null || state.bitCount < minState.bitCount) {
                minState = state
            }
        }
        return minState!!.toBooleanArrayList(text)
    }

    private fun updateStateListForChar(states: Collection<AztecState>, index: Int): Collection<AztecState> {
        val result = mutableListOf<AztecState>()
        for (state in states) {
            updateStateForChar(state, index, result)
        }
        return simplifyStates(result)
    }

    private fun updateStateForChar(state: AztecState, index: Int, result: MutableList<AztecState>) {
        val ch = (text[index].toInt() and 0xFF).toChar()
        val charInCurrentTable = CHAR_MAP[state.mode][ch.code] > 0
        var stateNoBinary: AztecState? = null

        for (mode in 0..4) {
            val charInMode = CHAR_MAP[mode][ch.code]
            if (charInMode > 0) {
                if (stateNoBinary == null) {
                    stateNoBinary = state.endBinaryShift(index)
                }
                if (!charInCurrentTable || mode == state.mode || mode == MODE_DIGIT) {
                    val res = stateNoBinary.latchAndAppend(mode, charInMode)
                    result.add(res)
                }
                if (!charInCurrentTable && SHIFT_TABLE[state.mode][mode] >= 0) {
                    val res = stateNoBinary.shiftAndAppend(mode, charInMode)
                    result.add(res)
                }
            }
        }
        if (state.binaryShiftByteCount > 0 || CHAR_MAP[state.mode][ch.code] == 0) {
            val res = state.addBinaryShiftChar(index)
            result.add(res)
        }
    }

    private fun updateStateListForPair(states: Collection<AztecState>, index: Int, pairCode: Int): Collection<AztecState> {
        val result = mutableListOf<AztecState>()
        for (state in states) {
            val stateNoBinary = state.endBinaryShift(index)
            result.add(stateNoBinary.latchAndAppend(MODE_PUNCT, pairCode))
            if (state.mode != MODE_PUNCT) {
                result.add(stateNoBinary.shiftAndAppend(MODE_PUNCT, pairCode))
            }
            if (pairCode == 3 || pairCode == 4) {
                val digitState = stateNoBinary
                    .latchAndAppend(MODE_DIGIT, 16 - pairCode)
                    .latchAndAppend(MODE_DIGIT, 1)
                result.add(digitState)
            }
            if (state.binaryShiftByteCount > 0) {
                result.add(state.addBinaryShiftChar(index).addBinaryShiftChar(index + 1))
            }
        }
        return simplifyStates(result)
    }

    private fun simplifyStates(states: Iterable<AztecState>): Collection<AztecState> {
        val result = mutableListOf<AztecState>()
        for (newState in states) {
            var add = true
            val it = result.iterator()
            while (it.hasNext()) {
                val oldState = it.next()
                if (oldState.isBetterThanOrEqualTo(newState)) {
                    add = false
                    break
                }
                if (newState.isBetterThanOrEqualTo(oldState)) {
                    it.remove()
                }
            }
            if (add) {
                result.add(newState)
            }
        }
        return result
    }

    companion object {
        const val MODE_UPPER = 0
        const val MODE_LOWER = 1
        const val MODE_MIXED = 2
        const val MODE_DIGIT = 3
        const val MODE_PUNCT = 4

        val LATCH_TABLE = arrayOf(
            intArrayOf(0, (5 shl 16) + 28, (5 shl 16) + 29, (5 shl 16) + 30, (10 shl 16) + (29 shl 5) + 30),
            intArrayOf((9 shl 16) + (30 shl 4) + 14, 0, (5 shl 16) + 29, (5 shl 16) + 30, (10 shl 16) + (29 shl 5) + 30),
            intArrayOf((5 shl 16) + 29, (5 shl 16) + 28, 0, (5 shl 16) + 30, (5 shl 16) + 30),
            intArrayOf((4 shl 16) + 14, (9 shl 16) + (14 shl 5) + 28, (9 shl 16) + (14 shl 5) + 29, 0, (9 shl 16) + (14 shl 5) + 30),
            intArrayOf((5 shl 16) + 31, (10 shl 16) + (31 shl 5) + 28, (10 shl 16) + (31 shl 5) + 29, (10 shl 16) + (31 shl 5) + 30, 0)
        )

        val SHIFT_TABLE = Array(6) { IntArray(6) { -1 } }.apply {
            this[MODE_UPPER][MODE_PUNCT] = 0
            this[MODE_MIXED][MODE_PUNCT] = 0
            this[MODE_LOWER][MODE_PUNCT] = 0
            this[MODE_LOWER][MODE_UPPER] = 28
        }

        val CHAR_MAP = Array(5) { IntArray(256) }.apply {
            this[MODE_UPPER][' '.code] = 1
            for (c in 'A'..'Z') {
                this[MODE_UPPER][c.code] = c - 'A' + 2
            }
            this[MODE_LOWER][' '.code] = 1
            for (c in 'a'..'z') {
                this[MODE_LOWER][c.code] = c - 'a' + 2
            }
            this[MODE_DIGIT][' '.code] = 1
            for (c in '0'..'9') {
                this[MODE_DIGIT][c.code] = c - '0' + 2
            }
            this[MODE_DIGIT][','.code] = 12
            this[MODE_DIGIT]['.'.code] = 13

            val mixedChars = intArrayOf(
                0, ' '.code, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10,
                11, 12, 13, 27, 28, 29, 30, 31, '@'.code, '\\'.code,
                '^'.code, '_'.code, '`'.code, '|'.code, '~'.code, 127
            )
            for (i in mixedChars.indices) {
                if (mixedChars[i] != 0) {
                    this[MODE_MIXED][mixedChars[i]] = i
                }
            }

            val punctChars = intArrayOf(
                0, '\r'.code, 0, 0, 0, 0, '!'.code, '"'.code,
                '#'.code, '$'.code, '%'.code, '&'.code, '\''.code, '('.code,
                ')'.code, '*'.code, '+'.code, ','.code, '-'.code, '.'.code,
                '/'.code, ':'.code, ';'.code, '<'.code, '='.code, '>'.code,
                '?'.code, '['.code, ']'.code, '{'.code, '}'.code
            )
            for (i in punctChars.indices) {
                if (punctChars[i] != 0) {
                    this[MODE_PUNCT][punctChars[i]] = i
                }
            }
        }
    }
}
