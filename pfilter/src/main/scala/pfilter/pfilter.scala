/*
pfilter.scala

Top level code for pfilter blog post

 */

object PFilter {

  import scala.language.higherKinds
  import scala.collection.parallel.immutable.ParVector
  //import cats.{Functor,Foldable,Eval}
  //import cats.Monoid
  //import cats.implicits._


  type LogWeight = Double
  type State = Int
  type Obs = Float

  // My generic collection typeclass
  trait GenericColl[C[_]] {
    def map[A,B](ca: C[A])(f: A => B): C[B]
    def reduce[A](ca: C[A])(f: (A,A) => A): A
    }

  // Implementation for Vector
  implicit val vGC: GenericColl[Vector] = new GenericColl[Vector] {
    def map[A,B](ca: Vector[A])(f: A=>B): Vector[B] = ca map f
    def reduce[A](ca: Vector[A])(f: (A,A)=>A): A = ca reduce f
    }

  // Implementation for ParVector
  implicit val pvGC: GenericColl[ParVector] = new GenericColl[ParVector] {
    def map[A,B](ca: ParVector[A])(f: A=>B): ParVector[B] = ca map f
    def reduce[A](ca: ParVector[A])(f: (A,A)=>A): A = ca reduce f
    }


  def main(args: Array[String]): Unit = {
    //import cats.functor
    println("Hi")
    val ed = Vector(5,10,15)
    println(ed)
    val edm = ed map (x=>x*2)
    println(edm)
    println("Bye")
  }

}

// eof

