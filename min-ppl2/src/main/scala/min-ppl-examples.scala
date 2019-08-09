/*
min-ppl-examples.scala

Examples for a simple SMC-based probabilistic programming language

*/

object MinPplExamples2 {

  import MinPpl2._
  import breeze.stats.{meanAndVariance => meanVar}
  import breeze.linalg.DenseVector


  // Zip vs flatMap
  def example1 = {

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
    example1
    // example2
    println("Bye")
  }

}

// eof

