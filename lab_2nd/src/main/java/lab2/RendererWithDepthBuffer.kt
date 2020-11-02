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

        // Чтобы были заметнее различия между фоном и наименьшей глубиной,
        // при нормализации глубины между ними вводится зазор dif = 0.2.
        // Фоновая глубина = 0, наименьшая нефоновая глубина = 0.2, наибольшая глубина = 1.
        val dif = 0.2
        val min = depthBuf.toList().minOf { it }
        val max = depthBuf.toList().maxOf { it }
        val scale = (1 - dif) / (max - min)

        depthPlot.drawFromBuffer(depthBuf) { depth ->
            val gray = if (depth == Double.NEGATIVE_INFINITY) {
                0.0
            } else {
                dif + (depth - min) * scale
            }
            Color(gray, 1.0, gray, 1.0)
        }
    }

    private fun processTriangle(v1: Vertex, v2: Vertex, v3: Vertex, rgbaColor: Int) {
        val normal = (v2 - v1) cross (v3 - v1)
        if (abs(normal.z) < almostZero) {
            return
        }

        val (a, b, c) = normal.toList()
        val d = (normal * v1).let { -(it.x + it.y + it.z) }
        val plainFactors = doubleArrayOf(a, b, c, d)

        val vertList = listOf(v1, v2, v3).sortedBy { it.y } // сверху вниз

        // верхняя половина треугольника
        val top = vertList.first()
        val (leftBot, rightBot) = vertList.subList(1, 3).sortedBy { it.x }
        processHalf(Segment(top, leftBot), Segment(top, rightBot), plainFactors, rgbaColor)

        // нижняя половина треугольника
        val bot = vertList.last()
        val (leftTop, rightTop) = vertList.subList(0, 2).sortedBy { it.x }
        processHalf(Segment(leftTop, bot), Segment(rightTop, bot), plainFactors, rgbaColor)
    }

    private fun processHalf(left: Segment, right: Segment, plainFactors: DoubleArray, rgba: Int) {
        val (a, b, c, d) = plainFactors
        val zShift = -a / c

        val leftXFunc = getYtoXFunc(left.first, left.second, colorBuf.cols-1)
        val rightXFunc = getYtoXFunc(right.first, right.second, colorBuf.cols-1)

        val startY = max(left.first.y, right.first.y).toInt().coerceIn(0, colorBuf.rows-1)
        val endY = min(left.second.y, right.second.y).toInt().coerceIn(0, colorBuf.rows-1)

        for (y in startY until endY) {
            val leftX = leftXFunc(y)
            val rightX = rightXFunc(y)
            var z = -(leftX * a + b*y + d) / c
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

private fun getYtoXFunc(start: Vertex, end: Vertex, maxValue: Int): (Int) -> Int {
    val startX = start.x.toInt()
    val startY = start.y.toInt()
    val endX = end.x.toInt()
    val endY = end.y.toInt()

    return getLinearFunc(startY, startX, endY, endX).let { xFunc ->
        { y -> xFunc(y).coerceIn(0, maxValue) }
    }
}

private fun getLinearFunc(x1: Int, y1: Int, x2: Int, y2: Int): (Int) -> Int {
    return {
        x -> y1 + (x - x1) * (y2 - y1) / (x2 - x1)
    }
}

private fun <T> Canvas.drawFromBuffer(buffer: Matrix<T>, valueToColor: (T) -> Color) {
    val writer = this.graphicsContext2D.pixelWriter
    buffer.forEachIndexed { x, y, value ->
        val color = valueToColor(value)
        val jfxColor = JFXColor(color.r, color.g, color.b, color.a)
        writer.setColor(x, y, jfxColor)
    }
}
