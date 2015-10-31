/*
currying.scala

*/

import breeze.plot._

object Currying
{

  def plotFun(fun: Double => Double, xmin: Double, xmax: Double): Figure = {
    val f = Figure()
    val p = f.subplot(0)
    import breeze.linalg._
    val x = linspace(xmin,xmax)
    p += plot(x, x map fun)
    p.xlabel = "x"
    p.ylabel = "f(x)"
    f
  }


  def main(args: Array[String]): Unit = {
    println("Hello")

    plotFun(x => x*x, -3, 3)
    def myQuad1(x: Double): Double = x*x - 2*x + 1
    plotFun(myQuad1, -3, 3)
    def myQuad2(x: Double): Double = x*x - 3*x - 1
    plotFun(myQuad2, -3, 3)
    def quadratic(a: Double, b: Double, c: Double, x: Double): Double = a*x*x + b*x + c
    def quadFun(a: Double, b: Double, c: Double): Double => Double = 
      x => quadratic(a,b,c,x)
    plotFun(quadFun(1,2,3),-3,3)
    def quad(a: Double, b: Double, c: Double)(x: Double): Double = a*x*x + b*x + c
    plotFun(quad(1,2,-3),-3,3)
    def quadCurried = (quadratic _).curried
    plotFun(quadCurried(1)(2)(3),-3,3)

    println("Goodbye")
  }  


}



// eof


