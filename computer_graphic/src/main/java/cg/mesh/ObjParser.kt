package cg.mesh

import cg.drawning3d.Color
import cg.math.toVector
import java.io.File

class ObjParser {
    private enum class Token(val str: String) {
        VERTEX("v "),
        FACE("f "),
        COLOR("### :color ")
    }

    fun parseMesh(filename: String): Mesh {
        val lines = File(filename).readLines()

        // take vertex coordinates form lines like:
        // v 763.35223388671875 0.27161681652069 -0.32321721315384
        val vertices = lines.filter { s -> s.startsWith(Token.VERTEX.str) }
                .map(getTokenParser(String::toDouble))
                .map { vertex -> List(4) {
                    i -> vertex.getOrElse(i){ 1.0 }
                } }
                .map { it.toVector() }

        // take face info (vertex ind, texture coordinate ind, normal ind) from lines like:
        // f 4/3/1 2/1/2 10/1/6
        val facesInfo = lines.filter { s -> s.startsWith(Token.FACE.str) }
                .map {
                    val faceInfo = getTokenParser(::wordToVertexInfo)(it)
                    if (faceInfo.size < 3) {
                        throw ObjFormatException("wrong face size: ${faceInfo.size}")
                    }
                    faceInfo
                }

        val faces = facesInfo.map { row -> row.map { it[0] } }

        val color = lines.find { it.startsWith(Token.COLOR.str) }
                ?.substring(Token.COLOR.str.length).orEmpty()
                .let { line ->
                    val number = "([0-9a-fA-F]{2})"
                    val regex = "#$number$number$number$number".toRegex()
                    regex.matchEntire(line)?.destructured?.toList()?.map { it.toInt(16) }
                            ?: listOf(0, 0, 0, 0xFF)
                }
                .map { it / 255.0 }
                .let { Color(it[0], it[1], it[2], it[3]) }

        return Mesh(vertices, faces, color)
    }
}

class ObjFormatException(msg: String): Exception(msg)

// converts String to List of type R
private fun <R> getTokenParser(transformToken: (String) -> R): (String) -> List<R> {
    return {
        s -> s.trim()
            .split(' ')
            .drop(1)
            .map(transformToken)
    }
}

// converts part of lines with faces info
//      1/2/3   to  array[0, 1, 2]
//      1/2     to  array[0, 1, -1]
//      1//3    to  array[0, -1, 2]
//      1       to  array[0, -1, -1]
private fun wordToVertexInfo(word: String): IntArray {
    val partialFaceData = word.split('/')
            .map { s -> (s.toIntOrNull() ?: 0) - 1 }
    val fullDataSize = 3 // vertex coordinates index + texture coordinates index + normal index
    return IntArray(fullDataSize) { i -> partialFaceData.getOrElse(i) { -1 } }
}