/*
LvRre2.scala

PDE approximation for a Lotka-Volterra system

Numerical solution of an SPDE

*/

package rd

object LvRre2 {

  import smfsb._
  import breeze.linalg.{Vector => BVec, _}
  import breeze.numerics._

  def main(args: Array[String]): Unit = {
    val r = 300; val c = 600
    val model = SpnModels.lv[DoubleState]()
    val step = Spatial.euler2d(model, DenseVector(0.6, 0.6), 0.05)
    val x00 = DenseVector(0.0, 0.0)
    val x0 = DenseVector(50.0, 100.0)
    val xx00 = PMatrix(r, c, Vector.fill(r*c)(x00))
    val xx0 = xx00.
      updated(c/3, r/2, x0).
      updated(2*c/3,r/2,x0).
      updated(c/2,2*r/3,x0)
    val s = Stream.iterate(xx0)(step(_,0.0,0.1))
    val si = s map (toSfxI(_))
    scalaview.SfxImageViewer(si, 1, autoStart=true)
  }

}

// eof
