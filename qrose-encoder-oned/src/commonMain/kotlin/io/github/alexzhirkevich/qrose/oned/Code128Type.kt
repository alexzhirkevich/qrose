package io.github.alexzhirkevich.qrose.oned

public enum class Code128Type(internal val v: Int) {
    A(Code128Encoder.CODE_CODE_A),
    B(Code128Encoder.CODE_CODE_B),
    C(Code128Encoder.CODE_CODE_C)
}