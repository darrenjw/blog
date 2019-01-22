/*
LvCle3.scala

Chemical Langevin approximation for a Lotka-Volterra system

Numerical solution of an SPDE

*/

package rd

object LvCle3 {

  import smfsb._
  import breeze.linalg.{Vector => BVec, _}
  import breeze.numerics._
  import breeze.stats.distributions.Uniform

  def main(args: Array[String]): Unit = {
    val r = 300; val c = 400
    val model = SpnModels.lv[DoubleState]()
    val step = Spatial.cle2d(model, DenseVector(0.6, 0.6), 0.05)
    val xx0 = PMatrix(r, c, Vector.fill(r*c)(DenseVector(
      Uniform(100,300).draw,
      Uniform(100,300).draw)))
    val s = Stream.iterate(xx0)(step(_,0.0,0.1))
    val si = s map (toSfxI(_))
    scalaview.SfxImageViewer(si, 1, autoStart=true)
  }

}

// eof
