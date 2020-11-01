package lab1

class AnimatorJFX(val time: Long, val step: Double) {
    fun animate(animate: (progress: Double) -> Unit) {
        animate(1.0)
    }
}