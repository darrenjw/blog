/*
LvExact.scala

Exact Gillespie simulation of the RDME for a Lotka-Volterra system

*/

package rd

object LvExact {

  import smfsb._
  import breeze.linalg.{Vector => BVec, _}
  import breeze.numerics._

  def main(args: Array[String]): Unit = {
    val r = 100; val c = 120
    val model = SpnModels.lv[IntState]()
    val step = Spatial.gillespie2d(model, DenseVector(0.6, 0.6), maxH=1e12)
    val x00 = DenseVector(0, 0)
    val x0 = DenseVector(50, 100)
    val xx00 = PMatrix(r, c, Vector.fill(r*c)(x00))
    val xx0 = xx00.updated(c/2, r/2, x0)
    val s = Stream.iterate(xx0)(step(_,0.0,0.1))
    val si = s map (toSfxIi(_))
    scalaview.SfxImageViewer(si, 1, autoStart=true)
  }

}

// eof
