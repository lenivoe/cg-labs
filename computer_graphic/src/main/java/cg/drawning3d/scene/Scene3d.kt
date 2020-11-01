package cg.drawning3d.scene

import cg.drawning3d.IRenderer
import cg.matrix.*
import cg.drawning3d.scene.IScene3d.ProjectionType
import kotlin.math.PI

class Scene3d(override val sceneObjects: List<SceneObject>, private val renderer: IRenderer) : IScene3d {
    override fun render() {
        renderer.clear()
        sceneObjects.forEach { it.render(renderer) }
        renderer.acceptRenderedGraphics()
    }

    override fun setProjection(type: ProjectionType) {
        val mat = when (type) {
            ProjectionType.ORTHOGRAPHIC -> orthographicProjectionMat()
            ProjectionType.ISOMETRIC -> isometricProjectionMat()
            ProjectionType.CABINET -> obliqueProjectionMat(0.5, PI/180 * 45)
            ProjectionType.PERSPECTIVE -> perspectiveProjectionMat(3.0)
        }
        renderer.projectionMat = mat
    }
}