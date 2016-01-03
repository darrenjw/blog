/*
monads.scala

First steps with monads in scala

*/

object FirstMonads {

  def main(args: Array[String]): Unit = {
    println("Hello")

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




    println("Goodbye")
  }

}

// eof

