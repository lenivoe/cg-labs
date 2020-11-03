package lab2

import cg.drawning3d.Color
import javafx.scene.canvas.Canvas
import kotlin.math.*
import cg.drawning3d.Colors
import cg.drawning3d.IRenderer
import cg.drawning3d.toColor
import cg.math.*
import cg.matrix.*
import cg.mesh.Mesh

typealias JFXColor = javafx.scene.paint.Color

typealias Vertex = Vector<Double>
typealias Segment = Pair<Vertex, Vertex>

val Vertex.x
    inline get() = this[0]
val Vertex.y
    inline get() = this[1]
val Vertex.z
    inline get() = this[2]
val Vertex.w
    inline get() = this[3]

private const val almostZero = 1e-6

@Suppress("MemberVisibilityCanBePrivate")
class RendererWithDepthBuffer(private val mainPlot: Canvas, private val depthPlot: Canvas): IRenderer {
    private val colorBuf: MutableMatrix<Int>
    private val depthBuf: MutableMatrix<Double>

    init {
        val width = mainPlot.width.toInt()
        val height = mainPlot.height.toInt()
        colorBuf = createMutableMatrix(width, height) { _, _ -> Colors.BLACK.color.toInt() }
        depthBuf = createMutableMatrix(width, height) { _, _ -> Double.NEGATIVE_INFINITY }
    }

    override var projectionMat: Matrix<Double> = orthographicProjectionMat()

    private var screenMat: Matrix<Double> = mainPlot.let {
        val scale = min(mainPlot.width, mainPlot.height) / 2
        val scaleMat = scaleMat(scale, scale, scale)
        val invertYMat = reflectionMat(CoordinatePlane.XOZ)
        val toCenterMat = translationMat(mainPlot.width / 2, mainPlot.height / 2, 0.0)

        scaleMat x invertYMat x toCenterMat
    }

    override fun clear(color: Color) {
        colorBuf.forEachIndexed{ x, y, _ -> colorBuf[x, y] = color.toInt() }
        depthBuf.forEachIndexed{ x, y, _ -> depthBuf[x, y] = Double.NEGATIVE_INFINITY }
    }

    override fun render(mesh: Mesh, modelViewMat: Matrix<Double>) {
        val transformMat = modelViewMat x projectionMat x screenMat

        val screenVertices = mesh.vertices.map {
            vertex -> (vertex x transformMat).let { it / it.w }
        }

        val triangulatedFaces = mesh.faces.asSequence().map { indices ->
            (2 until indices.size).asSequence().map {
                listOf(0, it-1, it).map { i -> screenVertices[indices[i]] }
            }
        }.flatten().toList()

        val color = mesh.color.toInt()
        for ((v1, v2, v3) in triangulatedFaces) {
            processTriangle(v1, v2, v3, color)
        }
    }

    override fun acceptRenderedGraphics() {
        mainPlot.drawFromBuffer(colorBuf, ::toColor)

        val zLength = 1.2 * max(depthBuf.cols, depthBuf.rows)
        depthPlot.drawFromBuffer(depthBuf) { depth ->
            val c = ((zLength + depth) / zLength).coerceIn(0.0, 1.0)
            Color(c, 0.8, c)
        }
    }

    private fun processTriangle(v1: Vertex, v2: Vertex, v3: Vertex, rgbaColor: Int) {
        val vertList = listOf(v1, v2, v3).sortedBy { it.y } // сверху вниз

        val (top, _, bot) = vertList
        if (abs(top.y - bot.y) < almostZero) {
            return
        }

        // разделить более длинное ребро горизонтальной линией,
        // проходящей через вторую по вертикали вершину
        var left = splitSegment(top, bot, vertList[1].y)
        var right = vertList[1]
        if (left.x > right.x) {
            left = right.also { right = left } // swap
        }

        val plainFactors = calcPlainFactors(v1, v2, v3)

        processHalf(Segment(top, left), Segment(top, right), plainFactors, rgbaColor)
        processHalf(Segment(left, bot), Segment(right, bot), plainFactors, rgbaColor)
    }

    private fun processHalf(left: Segment, right: Segment, plainFactors: DoubleArray, rgba: Int) {
        if (abs(left.first.y - left.second.y) < almostZero) {
            return
        }

        val (a, b, c, d) = plainFactors
        val zShift = -a / c

        val leftXFunc = getYtoXFunc(left.first, left.second, colorBuf.cols-1)
        val rightXFunc = getYtoXFunc(right.first, right.second, colorBuf.cols-1)

        val startY = floor(left.first.y).toInt().coerceIn(0, colorBuf.rows-1)
        val endY = ceil(left.second.y).toInt().coerceIn(0, colorBuf.rows-1)

        for (y in startY until endY) {
            val leftX = leftXFunc(y)
            val rightX = rightXFunc(y)
            var z = -(a * leftX + b * y + d) / c
            for (x in leftX until rightX) {
                if (depthBuf[x, y] < z) {
                    depthBuf[x, y] = z
                    colorBuf[x, y] = rgba
                }
                z += zShift
            }
        }
    }
}

private fun calcPlainFactors(v1: Vertex, v2: Vertex, v3: Vertex): DoubleArray {
    val normal = (v2 - v1) cross (v3 - v1)
    val (a, b, c) = normal.toList()
    val d = (normal * v1.subVector(0, 3)).let { -(it.x + it.y + it.z) }
    return doubleArrayOf(a, b, c, d)
}

private fun splitSegment(start: Vertex, end: Vertex, y: Double): Vertex {
    val xFunc = getLinearFunc(start.y, start.x, end.y, end.x)
    val zFunc = getLinearFunc(start.y, start.z, end.y, end.z)
    return listOf(xFunc(y), y, zFunc(y), 1.0).toVector()
}

private fun getYtoXFunc(start: Vertex, end: Vertex, maxValue: Int): (Int) -> Int {
    val bound = if (start.y < end.y) start.y..end.y else end.y..start.y
    return getLinearFunc(start.y, start.x, end.y, end.x).let { xFunc ->
        { y ->
            val arg = y.toDouble().coerceIn(bound)
            xFunc(arg).toInt().coerceIn(0, maxValue)
        }
    }
}


private fun getLinearFunc(x1: Double, y1: Double, x2: Double, y2: Double): (Double) -> Double {
    assert(abs(x2 - x1) >= almostZero) { "dividing by zero: ($x1, $y1) -> ($x2, $y2)" }
    return { x -> y1 + (x - x1) * (y2 - y1) / (x2 - x1) }
}

private fun <T> Canvas.drawFromBuffer(buffer: Matrix<T>, valueToColor: (T) -> Color) {
    val writer = this.graphicsContext2D.pixelWriter
    buffer.forEachIndexed { x, y, value ->
        val color = valueToColor(value)
        val jfxColor = JFXColor(color.r, color.g, color.b, color.a)
        writer.setColor(x, y, jfxColor)
    }
}
