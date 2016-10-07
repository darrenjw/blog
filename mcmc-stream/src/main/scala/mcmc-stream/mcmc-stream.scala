/*
mcmc-stream.scala


 */

import breeze.linalg._
import breeze.stats.distributions.{Gaussian, Uniform}
import breeze.plot._

object MCMC {

  def metrop(n: Int = 1000, eps: Double = 0.5): DenseVector[Double] = {
    val vec = DenseVector.fill(n)(0.0)
    var x = 0.0
    var oldll = Gaussian(0.0, 1.0).logPdf(x)
    vec(0) = x
    (1 until n).foreach { i =>
      val can = x + Uniform(-eps, eps).draw
      val loglik = Gaussian(0.0, 1.0).logPdf(can)
      val loga = loglik - oldll
      if (math.log(Uniform(0.0, 1.0).draw) < loga) {
        x = can
        oldll = loglik
      }
      vec(i) = x
    }
    vec
  }



  def main(arg: Array[String]): Unit = {
    println("Hi")

    val metropOut = metrop(10000, 1.0)

    val f=Figure()
    val p=f.subplot(0)
    p+=hist(metropOut,100)

    println("Bye")
  }

}

// eof

