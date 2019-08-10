/*
min-ppl-examples.scala

Examples for a simple SMC-based probabilistic programming language

*/

object MinPplExamples2 {

  import MinPpl2._
  import breeze.stats.{meanAndVariance => meanVar}
  import breeze.linalg.DenseVector
  import cats._
  import cats.implicits._
  import cats.syntax._

  // Zip vs flatMap
  def example1 = {
    println("binding with for")
    val prior1 = for {
      x <- Normal(0,1)
      y <- Gamma(1,1)
      z <- Poisson(10)
    } yield (x,y,z)
    println(meanVar(prior1.empirical.map(_._2)))
    println("binding with flatMap")
    val prior2 =
      Normal(0,1) flatMap {x =>
        Gamma(1,1) flatMap {y =>
          Poisson(10) map {z =>
            (x,y,z)}}}
    println(meanVar(prior2.empirical.map(_._2)))
    println("tupling")
    val prior3 = Applicative[Prob].tuple3(Normal(0,1), Gamma(1,1), Poisson(10))
    println(meanVar(prior3.empirical.map(_._2)))
    print("done")
  }

  // Poisson DGLM
  def example2 = {

    val data = List(2,1,0,2,3,4,5,4,3,2,1)

    val prior = for {
      w <- Gamma(1, 1)
      state0 <- Normal(0.0, 2.0)
    } yield (w, List(state0))
    
    def addTimePointSimple(current: Prob[(Double, List[Double])],
      obs: Int): Prob[(Double, List[Double])] = {
      println(s"Conditioning on observation: $obs")
      val updated = for {
        tup <- current
        (w, states) = tup
        os = states.head
        ns <- Normal(os, w)
        _ <- Poisson(math.exp(ns)).fitQ(obs)
      } yield (w, ns :: states)
      updated.resample
    }

    def addTimePoint(current: Prob[(Double, List[Double])],
      obs: Int): Prob[(Double, List[Double])] = {
      println(s"Conditioning on observation: $obs")
      val predict = for {
        tup <- current
        (w, states) = tup
        os = states.head
        ns <- Normal(os, w)
      }
      yield (w, ns :: states)
      val updated = for {
        tup <- predict
        (w, states) = tup
        st = states.head
        _ <- Poisson(math.exp(st)).fitQ(obs)
      } yield (w, states)
      updated.resample
    }

    val mod = data.foldLeft(prior)(addTimePoint(_,_)).empirical
    print("w  : ")
    println(meanVar(mod map (_._1)))
    print("s0 : ")
    println(meanVar(mod map (_._2.reverse.head)))
    print("sN : ")
    println(meanVar(mod map (_._2.head)))

  }



  // Main entry point

  def main(args: Array[String]): Unit = {
    println("Hi")
    //example1
    example2
    println("Bye")
  }

}

// eof

