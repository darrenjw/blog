/*
monads.scala

First steps with monads in scala

*/

object FirstMonads {

  def main(args: Array[String]): Unit = {

    println("Hello")

    println("map and Functors")
    val x = (0 to 4).toList
    println(x)
    val x2 = x map { x => x * 3 }
    println(x2)
    val x3 = x map { _ * 3 }
    println(x3)
    val x4 = x map { _ * 0.1 }
    println(x4)
    val xv = x.toVector
    println(xv)
    val xv2 = xv map { _ * 0.2 }
    println(xv2)
    val xv3 = for (xi <- xv) yield (xi * 0.2)
    println(xv3)

    println("flatMap and Monads")
    val x5 = x map { x => List(x - 0.1, x + 0.1) }
    println(x5)
    val x6 = x flatMap { x => List(x - 0.1, x + 0.1) }
    println(x6)

    val y = (0 to 12 by 2).toList
    println(y)
    val xy = x flatMap { xi => y map { yi => xi * yi } }
    println(xy)
    val xy2 = for {
      xi <- x
      yi <- y
    } yield (xi * yi)
    println(xy2)

    println("Option monad")
    val three = Option(3)
    val twelve = three map (_ * 4)
    println(twelve)
    val four = Option(4)
    val twelveB = three map (i => four map (i * _))
    println(twelveB)
    val twelveC = three flatMap (i => four map (i * _))
    println(twelveC)
    val twelveD = for {
      i <- three
      j <- four
    } yield (i * j)
    println(twelveD)
    val oops: Option[Int] = None
    val oopsB = for {
      i <- three
      j <- oops
    } yield (i * j)
    println(oopsB)
    val oopsC = for {
      i <- oops
      j <- four
    } yield (i * j)
    println(oopsC)

    println("IEEE floating point and NaN")
    val nan = Double.NaN
    println(3.0 * 4.0)
    println(3.0 * nan)
    println(nan * 4.0)
    val nanB = 0.0 / 0.0
    println(nanB)
    // val nanC=0/0
    // println(nanC)

    println("Option for matrix computations")
    import breeze.linalg._
    def safeChol(m: DenseMatrix[Double]): Option[DenseMatrix[Double]] = scala.util.Try(cholesky(m)).toOption
    val m = DenseMatrix((2.0, 1.0), (1.0, 3.0))
    val c = safeChol(m)
    println(c)
    val m2 = DenseMatrix((1.0, 2.0), (2.0, 3.0))
    val c2 = safeChol(m2)
    println(c2)

    import com.github.fommil.netlib.BLAS.{getInstance => blas}
    def dangerousForwardSolve(A: DenseMatrix[Double], y: DenseVector[Double]): DenseVector[Double] = {
      val yc = y.copy
      blas.dtrsv("L", "N", "N", A.cols, A.toArray, A.rows, yc.data, 1)
      yc
    }
    def safeForwardSolve(A: DenseMatrix[Double], y: DenseVector[Double]): Option[DenseVector[Double]] = scala.util.Try(dangerousForwardSolve(A, y)).toOption

    def safeStd(A: DenseMatrix[Double], y: DenseVector[Double]): Option[DenseVector[Double]] = for {
      L <- safeChol(A)
      z <- safeForwardSolve(L, y)
    } yield z

    println(safeStd(m,DenseVector(1.0,2.0)))

    println("Future monad")
    import scala.concurrent.duration._
    import scala.concurrent.{Future,ExecutionContext,Await}
    import ExecutionContext.Implicits.global
    val f1=Future{
      Thread.sleep(10000)
      1 }
    val f2=Future{
      Thread.sleep(10000)
      2 }
    val f3=for {
      v1<-f1
      v2<-f2
      } yield (v1+v2)
    println(Await.result(f3,30.second))

    println("Goodbye")
  }

}

// eof

