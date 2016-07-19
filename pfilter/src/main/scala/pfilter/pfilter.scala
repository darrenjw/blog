/*
pfilter.scala

Top level code for pfilter blog post

 */

object PFilter {

  import scala.language.higherKinds
  import scala.collection.parallel.immutable.ParVector
  import cats.{Functor,Foldable,Eval}
  //import cats.implicits._

  // Functor instance for ParVector (missing from cats.implicits._)
  implicit val pvFunctor: Functor[ParVector] = new Functor[ParVector] {
    def map[A,B](fa: ParVector[A])(f: A => B) = fa map f
  }

  // new typeclass I need...
  trait FoldableFunctor[F[_]] extends Functor[F] with Foldable[F]

  // FoldableFunctor instance for Vector
  implicit val vFoldFun: FoldableFunctor[Vector] = new FoldableFunctor[Vector] {
    def map[A,B](fa: Vector[A])(f: A => B) = fa map f
    def foldLeft[A, B](fa: Vector[A], b: B)(f: (B, A) => B): B = fa.foldLeft(b)(f)
    def foldRight[A, B](fa: Vector[A], lb: Eval[B])(f: (A, Eval[B]) => Eval[B]): Eval[B] = fa.foldRight(lb)(f)
    }


  type LogWeight = Double

  case class Particle[T](lw: LogWeight, v: T)

  case class EDist[T, C[_]: FoldableFunctor](pc: C[Particle[T]]) {

    def map[S](f: T => S): EDist[S, C] = {
      val mp = Functor[C].map(pc)(p => Particle(p.lw, f(p.v)))
      EDist[S, C](mp)
    }

  }

  def main(args: Array[String]): Unit = {
    import cats.functor
    println("Hi")
    val ed = EDist(Vector(Particle(0.3, 5), Particle(0.7, 10)))
    println(ed)
    val edm = ed map (x=>x*2)
    println(edm)
    println("Bye")
  }

}

// eof

