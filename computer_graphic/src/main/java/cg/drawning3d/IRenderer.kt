package cg.drawning3d

import cg.math.Matrix
import cg.mesh.Mesh

@Suppress("MemberVisibilityCanBePrivate")
interface IRenderer {
    fun clear(color: Color = Colors.WHITE.color)
    fun render(mesh: Mesh, modelViewMat: Matrix<Double>)
    fun acceptRenderedGraphics()

    var projectionMat: Matrix<Double>
}
