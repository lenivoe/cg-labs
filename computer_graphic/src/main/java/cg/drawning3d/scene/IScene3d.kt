package cg.drawning3d.scene

interface IScene3d {
    enum class ProjectionType { ORTHOGRAPHIC, ISOMETRIC, CABINET, PERSPECTIVE }

    fun render()
    fun setProjection(type: ProjectionType)

    val sceneObjects: List<SceneObject>
}