@file:Suppress("unused")

package cg.math

/**
 * Vector is Matrix[n x 1]
 */
interface Vector<T>: Matrix<T>

val <T> Vector<T>.size: Int
    get() = cols

operator fun <T> Vector<T>.get(i: Int): T = get(i, 0)


interface MutableVector<T>: Vector<T>, MutableMatrix<T>

operator fun <T> MutableVector<T>.set(i: Int, value: T) = set(i, 0, value)


class ListVector<T>(private val list: List<T>): AbstractMatrix<T>(), Vector<T> {
    override val cols: Int
        get() = list.size
    override val rows: Int
        get() = 1

    override fun get(x: Int, y: Int): T {
        assert(y == 1) { "vector has only one row" }
        return list[x]
    }

    override fun toList(): List<T> = list
}

class MutableListVector<T>(private val list: MutableList<T>): AbstractMatrix<T>(), MutableVector<T> {
    override val cols: Int
        get() = list.size
    override val rows: Int
        get() = 1

    override fun get(x: Int, y: Int): T {
        assert(y == 1) { "vector has only one row" }
        return list[x]
    }
    override fun set(x: Int, y: Int, value: T) {
        assert(y == 1) { "vector has only one row" }
        list[x] = value
    }

    override fun toList(): List<T> = list
}


fun <T> List<T>.toVector(): Vector<T> = ListVector(this)

fun <T> createVector(size: Int, init: (Int) -> T): Vector<T> =
        List(size, init).toVector()


fun <T> MutableList<T>.toMutableVector(): MutableVector<T> = MutableListVector(this)

fun <T> createMutableVector(size: Int, init: (Int) -> T): MutableVector<T> =
        MutableList(size, init).toMutableVector()


operator fun <M: Number, N: Number> Vector<M>.plus(other: Matrix<N>): Vector<Double> {
    return ((this as Matrix<M>) + other).toList().toVector()
}

operator fun <M: Number> Vector<M>.unaryMinus(): Vector<Double> {
    return -(this as Matrix<M>).toList().toVector()
}

operator fun <M: Number, N: Number> Vector<M>.minus(other: Matrix<N>): Vector<Double> {
    return ((this as Matrix<M>) - other).toList().toVector()
}

operator fun <M: Number, N: Number> Vector<M>.times(other: Matrix<N>): Vector<Double> {
    return ((this as Matrix<M>) * other).toList().toVector()
}

operator fun <M: Number> Vector<M>.times(other: Number): Vector<Double> {
    return ((this as Matrix<M>) * other).toList().toVector()
}

operator fun <M: Number> Number.times(other: Vector<M>): Vector<Double> {
    return (this * (other as Matrix<M>)).toList().toVector()
}

operator fun <M: Number> Vector<M>.div(other: Number): Vector<Double> {
    return ((this as Matrix<M>) / other).toList().toVector()
}

infix fun <M: Number, N: Number> Vector<M>.x(other: Matrix<N>): Vector<Double> {
    return ((this as Matrix<M>) x other).toList().toVector()
}

infix fun <M: Number, N: Number> Vector<M>.cross(other: Vector<N>): Vector<Double> {
    val (a1, a2, a3) = this.toList().map { it.toDouble() }
    val (b1, b2, b3) = other.toList().map { it.toDouble() }
    return listOf(a2*b3 - a3*b2, -a1*b3 + a3*b1, a1*b2 - a2*b1).toVector()
}
