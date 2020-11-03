@file:Suppress("unused")

package cg.math

operator fun <M: Number, N: Number> Matrix<M>.plus(other: Matrix<N>): Matrix<Double> {
    assert(rows == other.rows && cols == other.cols) { "Matrices not match" }

    return mapIndexed { x, y, value -> value.toDouble() + other[x, y].toDouble() }
}

operator fun <N: Number> Matrix<N>.unaryMinus(): Matrix<Double> {
    return map { -it.toDouble() }
}

operator fun <M: Number, N: Number> Matrix<M>.minus(other: Matrix<N>): Matrix<Double> {
    assert(rows == other.rows && cols == other.cols) { "Matrices not match" }

    return this + (-other)
}

operator fun <M: Number, N: Number> Matrix<M>.times(other: Matrix<N>): Matrix<Double> {
    assert(rows == other.rows && cols == other.cols) {
        "Matrices not match ([rows x cols]): [$rows x $cols] * [${other.rows} x ${other.cols}]"
    }

    return mapIndexed { x, y, value -> value.toDouble() * other[x, y].toDouble() }
}

operator fun <M: Number> Matrix<M>.times(other: Number): Matrix<Double> {
    return map { it.toDouble() * other.toDouble() }
}

operator fun <M: Number> Matrix<M>.div(other: Number): Matrix<Double> {
    return this * (1 / other.toDouble())
}

operator fun <M: Number> Number.times(other: Matrix<M>): Matrix<Double> {
    return other * this
}

/**
 * matrix multiplication operator
 */
infix fun <M: Number, N: Number> Matrix<M>.x(other: Matrix<N>): Matrix<Double> {
    assert(cols == other.rows) { "Matrices not match" }

    return createMatrix(cols = other.cols, rows = rows) { x, y ->
        var value = .0
        for (i in 0 until cols)
            value += this[i, y].toDouble() * other[x, i].toDouble()
        value
    }
}
