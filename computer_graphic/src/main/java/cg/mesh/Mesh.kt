package cg.mesh

import cg.drawning3d.Color
import cg.math.Vector

data class Mesh(val vertices: List<Vector<Double>>, val faces: List<List<Int>>, val color: Color)
