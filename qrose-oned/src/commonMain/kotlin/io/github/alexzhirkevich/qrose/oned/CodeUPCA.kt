package io.github.alexzhirkevich.qrose.oned

class CodeUPCA {
    fun encode(contents : String) = CodeEAN13().encode("0$contents")
}