package lab1

import cg.drawning3d.IRenderer
import cg.math.Matrix
import cg.math.times
import cg.math.x
import cg.matrix.*
import javafx.scene.canvas.Canvas
import javafx.scene.paint.Color
import cg.mesh.Mesh
import kotlin.math.*

typealias Point = Pair<Double, Double>

@Suppress("MemberVisibilityCanBePrivate")
class Renderer(private var plot: Canvas): IRenderer {
    override var projectionMat: Matrix<Double> = orthographicProjectionMat()

    private var screenMat: Matrix<Double> = plot.let {
        val scale = min(plot.width, plot.height) / 2
        val scaleMat = scaleMat(scale, scale, 1.0)
        val invertYMat = reflectionMat(CoordinatePlane.XOZ)
        val toCenterMat = translationMat(plot.width / 2, plot.height / 2, 0.0)

        scaleMat x invertYMat x toCenterMat
    }

    override fun clear(color: cg.drawning3d.Color) {
        val gc = plot.graphicsContext2D
        gc.fill = Color(color.r, color.g, color.b, color.a)
        gc.fillRect(0.0, 0.0, plot.width, plot.height)
    }

    override fun render(mesh: Mesh, modelViewMat: Matrix<Double>) {
        val transformMat = modelViewMat x projectionMat x screenMat
        val lines = getLines(mesh, transformMat)

        val gc = plot.graphicsContext2D
        gc.stroke = Color(mesh.color.r, mesh.color.g, mesh.color.b, mesh.color.a)

        for ((start, end) in lines) {
            val (x1, y1) = start
            val (x2, y2) = end
            gc.strokeLine(x1, y1, x2, y2)
        }
    }

    override fun acceptRenderedGraphics() { }


    private fun getLines(obj: Mesh, transformMat: Matrix<Double>): Sequence<Pair<Point, Point>> {
        return obj.faces.asSequence().flatMap { face ->
            face.map { vertexIndex ->
                val vertex = obj.vertices[vertexIndex] // index to vertex
                (vertex x transformMat).asScreenPoint() // to screen space
            }
                    .let { it.asSequence() + it.take(1).asSequence() } // list of points to loop
                    .zipWithNext()
        }
    }


    // convert vertex to screen point
    // vertex is vector of length = 4 or org.cg.matrix[1 x 4]
    private fun Matrix<Double>.asScreenPoint(): Point {
        assert(this.rows == 1 && this.cols == 4) { "need vector 4d" }

        val toCartesianCoordinates = 1 / this[3, 0]
        val point = this * toCartesianCoordinates
        return Point(point[0, 0], point[1, 0]) // from vector to screen point
    }
}
