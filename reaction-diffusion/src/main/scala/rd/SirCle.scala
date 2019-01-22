/*
SirCle.scala

Chemical Langevin approximation for a SIR epidemic model

Numerical solution of an SPDE

*/

package rd

object SirCle {

  import smfsb._
  import breeze.linalg.{Vector => BVec, _}
  import breeze.numerics._


  def main(args: Array[String]): Unit = {
    val r = 250; val c = 300
    val model = sir[DoubleState]()
    val step = Spatial.cle2d(model, DenseVector(3.0, 2.0, 0.0), 0.005)
    val x00 = DenseVector(100.0, 0.0, 0.0)
    val x0 = DenseVector(50.0, 50.0, 0.0)
    val xx00 = PMatrix(r, c, Vector.fill(r*c)(x00))
    val xx0 = xx00.updated(c/2, r/2, x0)
    val s = Stream.iterate(xx0)(step(_,0.0,0.05))
    val si = s map (toSfxI3(_))
    scalaview.SfxImageViewer(si, 1, autoStart=true)
  }

}

// eof
