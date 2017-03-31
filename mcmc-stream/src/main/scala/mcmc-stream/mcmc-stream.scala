/*
mcmc-stream.scala


 */

import breeze.linalg._
import breeze.plot._
import breeze.stats.distributions.{Gaussian, Uniform}
import breeze.stats.meanAndVariance
import annotation.tailrec

object MCMC {

  def mcmcSummary(dv: DenseVector[Double]): Figure = {
    val len = dv.length
    val mav = meanAndVariance(dv)
    val mean = mav.mean
    val variance = mav.variance
    println(s"Iters=$len, Mean=$mean, variance=$variance")
    val f = Figure("MCMC Summary")
    f.height = 1000
    f.width = 1200
    val p0 = f.subplot(1, 2, 0)
    p0 += plot(linspace(1, len, len), dv)
    p0.xlabel = "Iteration"
    p0.ylabel = "Value"
    p0.title = "Trace plot"
    val p1 = f.subplot(1, 2, 1)
    p1 += hist(dv, 100)
    p1.xlabel = "Value"
    p1.title = "Marginal density"
    f
  }

  def metrop1(n: Int = 1000, eps: Double = 0.5): DenseVector[Double] = {
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

  def metrop2(n: Int = 1000, eps: Double = 0.5): Unit = {
    var x = 0.0
    var oldll = Gaussian(0.0, 1.0).logPdf(x)
    (1 to n).foreach { i =>
      val can = x + Uniform(-eps, eps).draw
      val loglik = Gaussian(0.0, 1.0).logPdf(can)
      val loga = loglik - oldll
      if (math.log(Uniform(0.0, 1.0).draw) < loga) {
        x = can
        oldll = loglik
      }
      println(x)
    }
  }

  @tailrec
  def metrop3(n: Int = 1000, eps: Double = 0.5, x: Double = 0.0, oldll: Double = Double.MinValue): Unit = {
    if (n > 0) {
      println(x)
      val can = x + Uniform(-eps, eps).draw
      val loglik = Gaussian(0.0, 1.0).logPdf(can)
      val loga = loglik - oldll
      if (math.log(Uniform(0.0, 1.0).draw) < loga)
        metrop3(n - 1, eps, can, loglik)
      else
        metrop3(n - 1, eps, x, oldll)
    }
  }

  @tailrec
  def metrop4(n: Int = 1000, eps: Double = 0.5, x: Double = 0.0, oldll: Double = Double.MinValue, acc: List[Double] = Nil): DenseVector[Double] = {
    if (n == 0)
      DenseVector(acc.reverse.toArray)
    else {
      println(x)
      val can = x + Uniform(-eps, eps).draw
      val loglik = Gaussian(0.0, 1.0).logPdf(can)
      val loga = loglik - oldll
      if (math.log(Uniform(0.0, 1.0).draw) < loga)
        metrop4(n - 1, eps, can, loglik, can :: acc)
      else
        metrop4(n - 1, eps, x, oldll, x :: acc)
    }
  }

  def main(arg: Array[String]): Unit = {
    println("Hi")
    metrop1(10).foreach(println)
    //mcmcSummary(metrop1(100))
    metrop2(10)
    metrop3(10)
    //mcmcSummary(metrop4(1000))
    metrop4(10).foreach(println)
    println("Bye")
  }

}

// eof

