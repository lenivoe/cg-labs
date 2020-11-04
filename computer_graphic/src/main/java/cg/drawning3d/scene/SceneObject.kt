package cg.drawning3d.scene

import cg.drawning3d.IRenderer
import cg.math.Matrix
import cg.math.createMatrix
import cg.math.x
import cg.matrix.*
import cg.mesh.Mesh


class SceneObject(val mesh: Mesh): ISceneObject {
    private inner class State(modelMat: Matrix<Double>) : ISceneObject.IState {
        val oldModelMat = createMatrix(4, 4, modelMat.toList())

        override fun restore() {
            modelMat = createMatrix(4, 4, oldModelMat.toList())
        }
    }

    var modelMat = identityMat()
        set(mat) {
            assert(mat.rows == 4 && mat.cols == 4) {
                "model matrix must be [4 x 4], this matrix [${mat.rows} x ${mat.cols}]"
            }
            field = mat
        }


    override fun saveState(): ISceneObject.IState = State(modelMat)

    override fun render(renderer: IRenderer) = renderer.render(mesh, modelMat)

    override fun rotate(angle: Double, axis: Axis) {
        modelMat = modelMat x rotationMat(angle, axis)
    }

    override fun scale(x: Double, y: Double, z: Double) {
        modelMat = modelMat x scaleMat(x, y, z)
    }

    override fun translate(x: Double, y: Double, z: Double) {
        modelMat = modelMat x translationMat(x, y, z)
    }

    override fun reflect(plane: CoordinatePlane) {
        modelMat = modelMat x reflectionMat(plane)
    }

    override fun fitIntoScene() {
        val toSceneBox = getMatrixToFitIntoBox(mesh.vertices, modelMat)
        modelMat = modelMat x toSceneBox
    }
}