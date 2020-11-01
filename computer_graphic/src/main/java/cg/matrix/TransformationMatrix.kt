@file:Suppress("unused")

package cg.matrix

import cg.math.Matrix
import cg.math.createMatrix
import cg.math.x
import kotlin.math.*


enum class Axis { X, Y, Z }

enum class CoordinatePlane { YOZ,  XOZ,  XOY }


fun identityMat(): Matrix<Double> {
    return createMatrix(4, 4) { x, y -> if (x == y) 1.0 else 0.0 }
}

fun translationMat(x: Double, y: Double, z: Double): Matrix<Double> {
    return createMatrix(4, 4, listOf(
            1.0, 0.0, 0.0, 0.0,
            0.0, 1.0, 0.0, 0.0,
            0.0, 0.0, 1.0, 0.0,
              x,   y,   z, 1.0
    ))
}

fun reflectionMat(plane: CoordinatePlane): Matrix<Double> {
    val scale = Array(3) { if (it != plane.ordinal) 1.0 else -1.0 }
    return scaleMat(scale[0], scale[1], scale[2])
}

fun scaleMat(x: Double, y: Double, z: Double): Matrix<Double> {
    return createMatrix(4, 4, listOf(
              x, 0.0, 0.0, 0.0,
            0.0,   y, 0.0, 0.0,
            0.0, 0.0,   z, 0.0,
            0.0, 0.0, 0.0, 1.0
    ))
}

fun rotationMat(angle: Double, axis: Axis): Matrix<Double> {
    val c = cos(angle)
    val s = sin(angle)

    return when (axis) {
        Axis.X -> createMatrix(4, 4, listOf(
                1.0, 0.0, 0.0, 0.0,
                0.0,   c,   s, 0.0,
                0.0,  -s,   c, 0.0,
                0.0, 0.0, 0.0, 1.0
        ))
        Axis.Y -> createMatrix(4, 4, listOf(
                  c, 0.0,  -s, 0.0,
                0.0, 1.0, 0.0, 0.0,
                  s, 0.0,   c, 0.0,
                0.0, 0.0, 0.0, 1.0
        ))
        Axis.Z -> createMatrix(4, 4, listOf(
                  c,   s, 0.0, 0.0,
                 -s,   c, 0.0, 0.0,
                0.0, 0.0, 1.0, 0.0,
                0.0, 0.0, 0.0, 1.0
        ))
    }
}


fun orthographicProjectionMat(): Matrix<Double> = identityMat()

//fun orthographicProjectionMat(plane: CoordinatePlane): Matrix<Double> {
//    return createMatrix(4, 4) {
//        x, y -> if(x == y && x != plane.ordinal) 1.0 else 0.0
//    }
//}

fun obliqueProjectionMat(zScale: Double, angle: Double): Matrix<Double> {
    val a = -zScale * cos(angle)
    val b = -zScale * sin(angle)
    val matrixData = listOf(
            1.0, 0.0, 0.0, 0.0,
            0.0, 1.0, 0.0, 0.0,
            a,   b,   1.0, 0.0,  // a,   b,   1.0, 0.0,
            0.0, 0.0, 0.0, 1.0
    )
    return createMatrix(4, 4, matrixData)
}

fun perspectiveProjectionMat(distance: Double): Matrix<Double> {
    return createMatrix(4, 4, listOf(
            1.0, 0.0, 0.0, 0.0,
            0.0, 1.0, 0.0, 0.0,
            0.0, 0.0, 1.0, -1/distance,  // 0.0, 0.0, 0.0, -1/distance,
            0.0, 0.0, 0.0, 1.0
    ))
}

fun isometricProjectionMat(): Matrix<Double> {
    val toRad = PI/180
    val orthoZ = orthographicProjectionMat()
    val angleX = asin(tan(toRad * 30))
    val rotX = rotationMat(angleX, Axis.X)
    val rotY = rotationMat(toRad * -45, Axis.Y)
    return rotY x rotX x orthoZ
}
