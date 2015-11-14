/*
currying.scala
*/

import breeze.plot._

object Currying
{

  def plotFun(fun: Double => Double, xmin: Double = -3.0, xmax: Double = 3.0): Figure = {
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


    val l1 = List(1,2,3)
    val l2 = 4 :: l1
    println(l2)
    val l3 = l2 map { x => x*x }
    println(l3)
    val l4 = l2.map(x => x*x)
    println(l4)


    plotFun(x => x*x)

    def myQuad1(x: Double): Double = x*x - 2*x + 1
    plotFun(myQuad1)
    def myQuad2(x: Double): Double = x*x - 3*x - 1
    plotFun(myQuad2)

    val myQuad3: (Double => Double) = x => -x*x + 2
    plotFun(myQuad3)

    def quadratic(a: Double, b: Double, c: Double, x: Double): Double = a*x*x + b*x + c
    plotFun(x => quadratic(3,2,1,x))

    def quadFun(a: Double, b: Double, c: Double): Double => Double = 
      x => quadratic(a,b,c,x)
    val myQuad4 = quadFun(2,1,3)
    plotFun(myQuad4)
    plotFun(quadFun(1,2,3))

    val quadFunF: (Double,Double,Double) => Double => Double = 
      (a,b,c) => x => quadratic(a,b,c,x)
    val myQuad5 = quadFunF(-1,1,2)
    plotFun(myQuad5)
    plotFun(quadFunF(1,-2,3))

    val myQuad6 = quadratic(1,2,3,_: Double)
    plotFun(myQuad6)

    def quad(a: Double, b: Double, c: Double)(x: Double): Double = a*x*x + b*x + c
    plotFun(quad(1,2,-3))
    val myQuad7 = quad(1,0,1) _
    plotFun(myQuad7)

    def quadCurried = (quadratic _).curried
    plotFun(quadCurried(1)(2)(3))

    val quadraticF: (Double,Double,Double,Double) => Double = (a,b,c,x) => a*x*x + b*x + c
    def quadCurried2 = quadraticF.curried
    plotFun(quadCurried2(-1)(2)(3))

    println("Goodbye")
  }  

}

// eof

