package cg.matrix

import cg.math.*

typealias Vertex = Vector<Double>
typealias BoundBox = List<Pair<Double, Double>>

// vertices to cube (x: -1..1, y: -1..1, z: -1..0)
fun getMatrixToFitIntoBox(vertexList: List<Vertex>, modelMat: Matrix<Double>): Matrix<Double> {
    val bounds = listOf(Pair(-1.0, 1.0), Pair(-1.0, 1.0), Pair(-1.0, 0.0))
    return getMatrixToFitIntoBox(vertexList, modelMat, bounds)
}

fun getMatrixToFitIntoBox(vertexList: List<Vertex>, modelMat: Matrix<Double>, bounds: BoundBox): Matrix<Double> {
    if (vertexList.isEmpty()) {
        return identityMat()
    }

    val boxCenter = bounds.map { (start, end) -> (start + end) / 2 }
    val boxSize = bounds.map { (start, end) -> (end - start) }.minOf { it }

    val vertices = vertexList.map { v -> (v x modelMat).toList() }

    // (minX, maxX), (minY, maxY), (minZ, maxZ)
    val modelBounds = (0..2).map { i ->
        val min = vertices.minOf { it[i] / it[3] }
        val max = vertices.maxOf { it[i] / it[3] }
        Pair(min, max)
    }


    val shift = modelBounds.map { (min, max) -> -(min + max) / 2 }
    val toOriginPoint = translationMat(shift[0], shift[1], shift[2])

    val modelSize = modelBounds.map { (min, max) -> (max - min) }.maxOf { it }
    val scale = boxSize / modelSize
    val toBoxSize = scaleMat(scale, scale, scale)

    val toBoxCenter = translationMat(boxCenter[0], boxCenter[1], boxCenter[2])

    return toOriginPoint x toBoxSize x toBoxCenter
}