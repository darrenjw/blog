/*
rainier.scala
Stub for Scala Cats code
*/

object CatsApp {

  import cats._
  import cats.implicits._

  def main(args: Array[String]): Unit = {
    val l = List(1,2) |+| List(3,4)
    println(l)
  }

}
