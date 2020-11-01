package lab2

import javafx.scene.canvas.Canvas
import cg.drawning3d.scene.Scene3d
import cg.drawning3d.scene.SceneObject

class SceneBuilder(private val sceneObjects: List<SceneObject>) {
    fun buildScene(mainPlot: Canvas, depthPlot: Canvas): Scene3d {
        return Scene3d(sceneObjects, RendererWithDepthBuffer(mainPlot, depthPlot))
    }
}