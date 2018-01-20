# Comonads for statistical data processing

*This is a work-in-progress draft - final post will be posted on my wordpress.com blog*



```scala
import cats._
import cats.syntax._
import cats.instances._

// Stream Comonad instance - need to define "extract" and "coflatMap"...
// ASSUMING that the Stream is INFINTE
implict val streamComonad = new Comonad[Stream] {
  def extract[A](wa: Stream[A]) = wa.head
  def coflatMap[A,B](wa: Stream[A])(f: Stream[A] => B): Stream[B] = f(wa) #:: coflatMap(wa.tail)(f)
}


```












#### eof

