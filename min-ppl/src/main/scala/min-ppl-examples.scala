/*
min-ppl-examples.scala
Examples for a  minimal probabilistic programming language
*/

object MinPplExamples {

  import MinPpl._
  import breeze.stats.{meanAndVariance => meanVar}

  // Linear Gaussian
  def example1 = {
    val xy = for {
      x <- Normal(5,4)
      y <- Normal(x,1)
    } yield (x,y)
    val y = xy.map(_._2)
    val yGz = y.cond(yi => Normal(yi, 9).ll(8.0)).empirical
    print("y: 5.000, 5.000 : ")
    println(meanVar(y.empirical))
    print("y: 6.071, 3.214 : ")
    println(meanVar(yGz))
    val xyGz = xy.cond{case (x,y) => Normal(y,9).ll(8.0)}.empirical
    print("x: 5.857, 2.867 : ")
    println(meanVar(xyGz.map(_._1))) // x
    print("y: 6.071, 3.214 : ")
    println(meanVar(xyGz.map(_._2))) // y
    // Now cond inside for expression...
    val xyz = for {
      x <- Normal(5,4)
      y <- Normal(x,1).cond(y => Normal(y,9).ll(8.0))
    } yield (x,y)
    val xyze = xyz.empirical
    print("x: 5.857, 2.867 : ")
    println(meanVar(xyze.map(_._1))) // x
    print("y: 6.071, 3.214 : ")
    println(meanVar(xyze.map(_._2))) // y
    // Now cond inside a deeper for expression...
    val wxyz = for {
      w <- Normal(5,2)
      x <- Normal(w,2)
      y <- Normal(x,1).cond(y => Normal(y,9).ll(8.0))
    } yield (w,x,y)
    val wxyze = wxyz.empirical
    print("w: 5.429, 1.714 : ")
    println(meanVar(wxyze.map(_._1))) // w
    print("x: 5.857, 2.867 : ")
    println(meanVar(wxyze.map(_._2))) // x
    print("y: 6.071, 3.214 : ")
    println(meanVar(wxyze.map(_._3))) // y
    // Now fit...
    val xyzf = for {
      x <- Normal(5,4)
      y <- Normal(x,1)
      z <- Normal(y,9).fit(8.0)
    } yield (x,y,z)
    val xyzfe = xyzf.empirical
    print("x: 5.857, 2.867 : ")
    println(meanVar(xyzfe.map(_._1))) // x
    print("y: 6.071, 3.214 : ")
    println(meanVar(xyzfe.map(_._2))) // y
    print("z: 6.071,12.214 : ")
    println(meanVar(xyzfe.map(_._3))) // z
    // Now fitQ...
    val xyzfq = for {
      x <- Normal(5,4)
      y <- Normal(x,1)
      z <- Normal(y,9).fitQ(8.0)
    } yield (x,y,z)
    val xyzfqe = xyzfq.empirical
    print("x: 5.857, 2.867 : ")
    println(meanVar(xyzfqe.map(_._1))) // x
    print("y: 6.071, 3.214 : ")
    println(meanVar(xyzfqe.map(_._2))) // y
    print("z: 8.000, 0.000 : ")
    println(meanVar(xyzfqe.map(_._3))) // z
    // Simpler fit test
    val yzf = for {
      y <- Normal(5,5)
      z <- Normal(y,9).fit(8.0)
    } yield (y,z)
    val yzfe = yzf.empirical
    print("y: 6.071, 3.214 : ")
    println(meanVar(yzfe.map(_._1))) // y
    print("z: 6.071,12.214 : ")
    println(meanVar(yzfe.map(_._2))) // z
    // Simpler fit test - multiple obs
    val yzf2 = for {
      y <- Normal(5,5)
      z <- Normal(y,18).fit(List(6.0,10.0))
    } yield (y,z)
    val yzfe2 = yzf2.empirical
    print("y: 6.071, 3.214 : ")
    println(meanVar(yzfe2.map(_._1))) // y
    print("z: 6.071,21.214 : ")
    println(meanVar(yzfe2.map(_._2))) // z
  }

  def example1a = {
    val deep = for {
      w <- Normal(2.0,1.0)
      x <- Normal(w,1)
      y <- Normal(x,2)
      z <- Normal(y,1)
    } yield z
    println("2.0, 5.0 :")
    println(meanVar(deep.empirical))
  }

  // Normal random sample
  def example2 = {
    val mod = for {
      mu <- Normal(0,100)
      v <- Gamma(1,0.1)
      _ <- Normal(mu,v).fitQ(List(8.0,9,7,7,8,10))
    } yield (mu,v)
    val modEmp = mod.empirical
    print("mu : ")
    println(meanVar(modEmp map (_._1)))
    print("v : ")
    println(meanVar(modEmp map (_._2)))
  }

  // Normal random sample - IG on v
  def example2a = {
    val mod = for {
      mu <- Normal(0, 100)
      tau <- Gamma(1, 0.1)
      _ <- Normal(mu, 1.0/tau).fitQ(List(8.0,9,7,7,8,10))
    } yield (mu,tau)
    val modEmp = mod.empirical
    print("mu : ")
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
    //example1
    //example1a
    //example2
    //example2a
    //example3
    //example4
    example5
    println("Bye")
  }

}

// eof

