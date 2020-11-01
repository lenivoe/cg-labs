package lab1

import javafx.scene.canvas.Canvas
import cg.drawning3d.scene.Scene3d
import cg.drawning3d.scene.SceneObject

class SceneBuilder(private val sceneObjects: List<SceneObject>) {
    fun buildScene(plot: Canvas): Scene3d {
        return Scene3d(sceneObjects, Renderer(plot))
    }
}