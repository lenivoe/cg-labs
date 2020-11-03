package cg.drawning3d


@Suppress("unused")
enum class Colors(val color: Color) {
    WHITE(Color(1.0, 1.0, 1.0, 1.0)),
    BLACK(Color(0.0, 0.0, 0.0, 1.0)),
    RED(Color(1.0, 0.0, 0.0, 1.0)),
    GREEN(Color(0.0, 1.0, 0.0, 1.0)),
    BLUE(Color(0.0, 0.0, 1.0, 1.0)),
}

data class Color(val r: Double, val g: Double, val b: Double, val a: Double = 1.0) {
    fun toInt(): Int {
        val r = toByte(r) shl 24
        val g = toByte(g) shl 16
        val b = toByte(b) shl 8
        val a = toByte(a)

        return r or g or b or a
    }
}

fun toColor(rgba: Int): Color {
    val r = (rgba ushr 24) / 255.0
    val g = ((rgba ushr 16) and 0xFF) / 255.0
    val b = ((rgba ushr 8) and 0xFF) / 255.0
    val a = (rgba and 0xFF) / 255.0

    return Color(r, g, b, a)
}

private fun toByte(component: Double): Int
        = (component * 255).toInt()
