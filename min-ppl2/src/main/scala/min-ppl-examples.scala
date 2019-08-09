/*
min-ppl-examples.scala
Examples for a minimal probabilistic programming language
*/

object MinPplExamples2 {

  import MinPpl2._
  import breeze.stats.{meanAndVariance => meanVar}
  import breeze.linalg.DenseVector

  // Normal random sample
  def example1 = {
    val mod = for {
      mu <- Normal(0,100)
      v <- Gamma(1,0.1)
      _ <- Normal(mu,v).fitQ(List(8.0,9,7,7,8,10))
    } yield (mu,v)
    val modEmp = mod.empirical
    print("mu : ")
    println(meanVar(modEmp map (_._1)))
    print("v  : ")
    println(meanVar(modEmp map (_._2)))
  }

  // Normal random sample - IG on v
  def example2 = {
    val mod = for {
      mu <- Normal(0, 100)
      tau <- Gamma(1, 0.1)
      _ <- Normal(mu, 1.0/tau).fitQ(List(8.0,9,7,7,8,10))
    } yield (mu,tau)
    val modEmp = mod.empirical
    print("mu  : ")
    println(meanVar(modEmp map (_._1)))
    print("tau : ")
    println(meanVar(modEmp map (_._2)))
  }

  // Poisson DGLM
  def example3 = {

    val data = List(2,1,0,2,3,4,5,4,3,2,1)

    val prior = for {
      w <- Gamma(1, 1)
      state0 <- Normal(0.0, 2.0)
    } yield (w, List(state0))
    
    def addTimePoint(current: Prob[(Double, List[Double])],
      obs: Int): Prob[(Double, List[Double])] = {
      println(s"Conditioning on observation: $obs")
      for {
        tup <- current
        (w, states) = tup
        os = states.head
        ns <- Normal(os, w)
        _ <- Poisson(math.exp(ns)).fitQ(obs)
      } yield (w, ns :: states)
    }

    val mod = data.foldLeft(prior)(addTimePoint(_,_)).empirical
    print("w  : ")
    println(meanVar(mod map (_._1)))
    print("s0 : ")
    println(meanVar(mod map (_._2.reverse.head)))
    print("sN : ")
    println(meanVar(mod map (_._2.head)))

  }

  // Linear model
  def example4 = {
    val x = List(1.0,2,3,4,5,6)
    val y = List(3.0,2,4,5,5,6)
    val xy = x zip y
    case class Param(alpha: Double, beta: Double, v: Double)
    println("Forming prior distribution")
    val prior = for {
      alpha <- Normal(0,10)
      beta <- Normal(0,4)
      v <- Gamma(1,0.1)
    } yield Param(alpha, beta, v)
    def addPoint(current: Prob[Param], obs: (Double, Double)): Prob[Param] = {
      println(s"Conditioning on $obs")
      for {
        p <- current
        (x, y) = obs
        _ <- Normal(p.alpha + p.beta * x, p.v).fitQ(y)
      } yield p
    }
    val mod = xy.foldLeft(prior)(addPoint(_,_)).empirical
    print("a : ")
    println(meanVar(mod map (_.alpha)))
    print("b : ")
    println(meanVar(mod map (_.beta)))
    print("v : ")
    println(meanVar(mod map (_.v)))
  }

  // Noisy observations of a count
  def example5 = {
    val mod = for {
      count <- Poisson(10)
      tau <- Gamma(1,0.1)
      _ <- Normal(count,1.0/tau).fitQ(List(4.2,5.1,4.6,3.3,4.7,5.3))
    } yield (count,tau)
    val modEmp = mod.empirical
    print("count : ")
    println(meanVar(modEmp map (_._1.toDouble)))
    print("tau : ")
    println(meanVar(modEmp map (_._2)))
  }


  // Main entry point

  def main(args: Array[String]): Unit = {
    println("Hi")
    example1
    example2
    example3
    example4
    example5
    println("Bye")
  }

}

// eof

