# Bayesian hierarchical modelling with Rainier

## Introduction

In the [previous post](https://darrenjw.wordpress.com/2018/06/01/monadic-probabilistic-programming-in-scala-with-rainier/) I gave a brief introduction to [Rainier](https://github.com/stripe/rainier), a new HMC-based probabilistic programming library/DSL for Scala. In that post I assumed that people were using the latest source version of the library. Since then, version 0.1.1 of the library has been released, so in this post I will demonstrate use of the released version of the software (using the binaries published to Sonatype), and will walk through a slightly more interesting example - a dynamic linear state space model with unknown static parameters. This is similar to, but slightly different from, the DLM example in the Rainier library. So to follow along with this post, all that is required is [SBT](https://www.scala-sbt.org/).

## An interactive session

First run SBT from an empty directory, and paste the following at the SBT prompt:

```scala
set libraryDependencies  += "com.stripe" %% "rainier-plot" % "0.1.1"
set scalaVersion := "2.12.4"
console
```

This should give a Scala REPL with appropriate dependencies (`rainier-plot` has all of the relevant transitive dependencies). We'll begin with some imports, and then simulating some synthetic data from a dynamic linear state space model with an [AR(1)](https://en.wikipedia.org/wiki/Autoregressive_model) latent state and Gaussian noise on the observations.

```scala
import com.stripe.rainier.compute._
import com.stripe.rainier.core._
import com.stripe.rainier.sampler._

implicit val rng = ScalaRNG(1)
val n = 75 // number of observations/time points
val mu = 3.0 // AR(1) mean
val a = 0.95 // auto-regressive parameter
val sig = 1.0 // AR(1) SD
val sigD = 3.0 // observational SD
val state = Stream.
  iterate(0.0)(x => mu + (x - mu) * a + sig * rng.standardNormal).
  take(n).
  toVector
val obs = state.map(_ + sigD * rng.standardNormal)
```

Now we have some synthetic data, let's think about building a probabilistic program for this model. Start with a prior.

```scala
case class Static(mu: Real, a: Real, sig: Real, sigD: Real)
val prior = for {
  mu <- Normal(0, 10).param
  a <- Normal(1, 0.1).param
  sig <- Gamma(2,1).param
  sigD <- Gamma(2,2).param
  sp <- Normal(0, 50).param
} yield (Static(mu, a, sig, sigD), List(sp))
```

Note the use of a case class for wrapping the static parameters. Next, let's define a function to add a state and associated observation to an existing model.

```scala
def addTimePoint(current: RandomVariable[(Static, List[Real])],
                     datum: Double) = for {
  tup <- current
  static = tup._1
  states = tup._2
  os = states.head
  ns <- Normal(((Real.one - static.a) * static.mu) + (static.a * os),
                 static.sig).param
  _ <- Normal(ns, static.sigD).fit(datum)
} yield (static, ns :: states)
```

Given this, we can generate the probabilistic program for our model as a *fold* over the data initialised with the prior.

```scala
val fullModel = obs.foldLeft(prior)(addTimePoint(_, _))
```

If we don't want to keep samples for all of the states, we can focus on the parameters of interest, wrapping the results in a `Map` for convenient sampling and plotting.

```scala
val model = for {
  tup <- fullModel
  static = tup._1
  states = tup._2
} yield
  Map("mu" -> static.mu,
  "a" -> static.a,
  "sig" -> static.sig,
  "sigD" -> static.sigD,
  "SP" -> states.reverse.head)
```

We can sample with

```scala
val out = model.sample(HMC(3), 100000, 10000 * 500, 500)
```

(this will take a few minutes) and plot some diagnostics with

```scala
    import com.cibo.evilplot.geometry.Extent
    import com.stripe.rainier.plot.EvilTracePlot._

    render(traces(out, truth = Map("mu" -> mu,
      "a" -> a, "sigD" -> sigD, "sig" -> sig,
      "SP" -> state(0))), "traceplots.png",
           Extent(1200, 1400))
    render(pairs(out, truth = Map("mu" -> mu,
       "a" -> a, "sigD" -> sigD, "sig" -> sig,
       "SP" -> state(0))), "pairs.png")
```

This generates the following diagnostic plots:

![Trace plots]()

![Pairs plots]()




#### eof

