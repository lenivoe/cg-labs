package cg.drawning3d.scene

import cg.drawning3d.IRenderer
import cg.matrix.Axis
import cg.matrix.CoordinatePlane


interface ISceneObject {
    interface IState {
        fun restore()
    }

    fun saveState(): IState

    fun render(renderer: IRenderer)
    fun rotate(angle: Double, axis: Axis)
    fun scale(x: Double, y: Double, z: Double)
    fun translate(x: Double, y: Double, z: Double)
    fun reflect(plane: CoordinatePlane)
    fun fitIntoScene()
}