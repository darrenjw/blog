# Write your own general purpose monadic probabilistic programming language from scratch in 50 lines of (Scala) code

## Background

In May I attended a great workshop on [advances and challenges in machine learning languages
](https://yebai.github.io/mll-workshop/) at the CMS in Cambridge. There was an a good mix of people from different disciplines, and a bit of a theme around probabilistic programming. The [workshop schedule](https://yebai.github.io/mll-workshop/schedule.html) includes links to many of the presentations, and is generally worth browsing. In particular, it includes a link to the slides for my presentation on [a compositional approach to scalable Bayesian computation and probabilistic programming](https://drive.google.com/file/d/1c8o_K187x9QpKB1p2QFu7VRAnMiskcYU/view?usp=sharing). I've given a few talks on this kind of thing over the last couple of years, at Newcastle, at the Isaac Newton Institute in Cambridge (twice), and at CIRM in France. But I think I explained things best at this workshop at the CMS, though my impression could partly have been a reflection of the more interested and relevant audience. In the talk I started with a basic explanation of why ideas from [category theory](https://en.wikipedia.org/wiki/Category_theory) and [functional programming](https://en.wikipedia.org/wiki/Functional_programming) can help to solve problems in statistical computing in a more composable and scalable way, before moving on to discuss probability monads and their fundamental connection to probabilistic programming. The take home message from the talk is that if you have a generic inference algorithm, expressing the logic in the context of a probability monads can give you an embedded probabilistic programming language (PPL) for that inference algorithm essentially "for free".

So, during my talk I said something a little foolhardy. I can't remember my exact words, but while presenting the idea behind an [SMC](https://en.wikipedia.org/wiki/Particle_filter)-based probability monad I said something along the lines of "*one day I will write a blog post on how to write a probabilistic programming language from scratch in 50 lines of code, and this is how I'll do it*"! Rather predictably (with hindsight), immediately after my talk about half a dozen people all pleaded with me to urgently write the post! I've been a little busy since then, but now that things have settled down a little for the summer, I've some time to think and code, so here is that post.

## Introduction

The idea behind this post is to show that, if you think about the problem in the right way, and use a programming language with syntactic support for monadic compostion, then producing a flexible, general, compositional, embedded [domain specific language](https://en.wikipedia.org/wiki/Domain-specific_language) (DSL) for [probabilistic programming](https://en.wikipedia.org/wiki/Probabilistic_programming) based on a given generic inference algorithm is no more effort than hard-coding two or three illustrative examples. You would need to code up two or three examples for a paper anyway, but providing a PPL is *way* more useful. There is also an interesting converse to this, which is that if you *can't* easily produce a PPL for your "general" inference algorithm, then perhaps it isn't quite as "general" as you thought. I'll try to resist exploring that here...

To illustrate these principles I wan't to develop a fairly *minimal* PPL, so that the complexities of the inference algorithm don't hide the simplicity of the PPL embedding. [Importance sampling](https://en.wikipedia.org/wiki/Importance_sampling) with resampling is probably the simplest useful generic Bayesian inference algorithm to implement, so that's what I'll use. Note that there are many limitations of the approach that I will adopt, which will make it completely unsuitable for "real" problems. In particular, this implementation is: inefficient, in terms of both compute time and memory usage, statistically inefficient for deep nesting and repeated conditioning, due to the particle degeneracy problem, specific to a particular probability monad, strictly evaluated, impure (due to mutation of global random number state), etc. All of these things are easily fixed, but all at the expense of greater abstraction, complexity and lines of code. I'll probably discuss some of these generalisations and improvements in future posts, but for this post I want to keep everything as short and simple as practical. It's also worth mentioning that there is nothing particularly original here. Many people have written about monadic embedded PPLs, and several have used an SMC-based monad for illustration. I'll give some pointers to useful further reading at the end.

## The language, in 50 lines of code

Without further ado, let's just write the PPL. I'm using plain Scala, with just a dependency on the [Breeze](https://github.com/scalanlp/breeze) scientific library, which I'm going to use for simulating random numbers from standard distributions, and evaluation of their log densities. I have a directory of materials associated with this post in a [git repo](https://github.com/darrenjw/blog/tree/master/min-ppl). This post is derived from an executable [tut](http://tpolecat.github.io/tut/) document (so you know it works), which can be found [here](https://github.com/darrenjw/blog/blob/master/min-ppl/src/main/tut/DraftPost.md). If you just want to follow along copying code at the command prompt, just run [sbt](https://www.scala-sbt.org/) from an empty or temp directory, and copy the following to spin up a Scala console with the Breeze dependency:
```scala
set libraryDependencies += "org.scalanlp" %% "breeze" % "1.0-RC4"
set libraryDependencies += "org.scalanlp" %% "breeze-natives" % "1.0-RC4"
set scalaVersion := "2.13.0"
console
```

We start with a couple of Breeze imports
```tut:silent
import breeze.stats.{distributions => bdist}
import breeze.linalg.DenseVector
```
which are not strictly necessary, but clean up the subsequent code. We are going to use a set of weighted particles to represent a probability distribution empirically, so we'll start by defining an approriate [ADT](https://en.wikipedia.org/wiki/Algebraic_data_type) for these:
```tut:silent
implicit val numParticles = 300

case class Particle[T](v: T, lw: Double) { // value and log-weight
  def map[S](f: T => S): Particle[S] = Particle(f(v), lw)
}
```
We also include a `map` method for pushing a particle through a transformation, and a default number of particles for sampling and resampling. 300 partciles are enough for illustrative purposes. Ideally it would be good to increase this for more realistic experiments. We can use this particle type to build our main probability monad as follows.
```tut:silent
object Wrapped {

trait Prob[T] {
  val particles: Vector[Particle[T]]
  def map[S](f: T => S): Prob[S] = Empirical(particles map (_ map f))
  def flatMap[S](f: T => Prob[S]): Prob[S] = {
    Empirical((particles map (p => {
      f(p.v).particles.map(psi => Particle(psi.v, p.lw + psi.lw))
    })).flatten).resample
  }
  def resample(implicit N: Int): Prob[T] = {
    val lw = particles map (_.lw)
    val mx = lw reduce (math.max(_,_))
    val rw = lw map (lwi => math.exp(lwi - mx))
    val law = mx + math.log(rw.sum/(rw.length))
    val ind = bdist.Multinomial(DenseVector(rw.toArray)).sample(N)
    val newParticles = ind map (i => particles(i))
    Empirical(newParticles.toVector map (pi => Particle(pi.v, law)))
  }
  def cond(ll: T => Double): Prob[T] =
    Empirical(particles map (p => Particle(p.v, p.lw + ll(p.v))))
  def empirical: Vector[T] = resample.particles.map(_.v)
}

case class Empirical[T](particles: Vector[Particle[T]]) extends Prob[T]

}
import Wrapped._
```
Note that if you are pasting into the Scala REPL you will need to use `:paste` mode for this. So `Prob[_]` is our base probability monad trait, and `Empirical[_]` is our simplest implementation, which is just a collection of weighted particles. The method `flatMap` forms the naive product of empirical measures and then resamples in order to stop an explosion in the number of particles. There are two things worth noting about the `resample` method. The first is that the log-sum-exp trick is being used to avoid overflow and underflow when the log weights are exponentiated. The second is that although the method returns an equally weighted set of particles, the log weights are all set in order that the average raw weight of the output set matches the average raw weight of the input set. This is a little tricky to explain, but it turns out to be necessary in order to correctly propagate conditioning information back through multiple monadic binds (`flatMaps`). The `cond` method allows conditioning of a distribution using an arbitrary log-likelihood. It is included for comparison with some other implementations I will refer to later, but we won't actually be using it, so we could save two lines of code here if necessary.

It will be handy to have a function to turn a bunch of unweighted particles into a set of particles with equal weights, so we can define that as follows.
```tut:silent
def unweighted[T](ts: Vector[T], lw: Double = 0.0): Prob[T] =
  Empirical(ts map (Particle(_, lw)))
```
Probabilistic programming is essentially trivial if we only care about forward sampling. But interesting PPLs allow us to *condition* on observed values of random variables. In the context of SMC, this is simplest when the distribution being conditioned has a tractable log-likelihood. So we can now define an extension of our probability monad for distributions with a tractable log-likelihood, and define a bunch of convenient conditioning (or "fitting") methods using it.
```tut:silent
trait Dist[T] extends Prob[T] {
  def ll(obs: T): Double
  def ll(obs: Seq[T]): Double = obs map (ll) reduce (_+_)
  def fit(obs: Seq[T]): Prob[T] =
    Empirical(particles map (p => Particle(p.v, p.lw + ll(obs))))
  def fitQ(obs: Seq[T]): Prob[T] = Empirical(Vector(Particle(obs.head, ll(obs))))
  def fit(obs: T): Prob[T] = fit(List(obs))
  def fitQ(obs: T): Prob[T] = fitQ(List(obs))
}
```
The `fit` method re-weights a particle set according to the observed log-likelihood. For convenience, it also returns a particle cloud representing the posterior-predictive distribution of an iid value from the same distribution. This is handy, but comes at the expense of introducing and additional partcile cloud. So, if you aren't interested in the posterior predictive, you can avoid this cost by using the `fitQ` method (for "fit quick"), which doesn't return anything useful. We'll see examples of this in practice, shortly. Note that the `fitQ` methods aren't strictly required for our "minimal" PPL, so we can save a couple of lines by omitting them if necessary. Similarly for the variants which allow conditioning on a collection of iid observations from the same distribution.

At this point we are essentially done. But for convenience, we can define a few standard distributions to help get new users of our PPL started. Of course, since the PPL is embedded, it is trivial to add our own additional distributions later.
```tut:silent
case class Normal(mu: Double, v: Double)(implicit N: Int) extends Dist[Double] {
  lazy val particles = unweighted(bdist.Gaussian(mu, math.sqrt(v)).sample(N).toVector).particles
  def ll(obs: Double) = bdist.Gaussian(mu, math.sqrt(v)).logPdf(obs)
}

case class Gamma(a: Double, b: Double)(implicit N: Int) extends Dist[Double] {
  lazy val particles = unweighted(bdist.Gamma(a, 1.0/b).sample(N).toVector).particles
  def ll(obs: Double) = bdist.Gamma(a, 1.0/b).logPdf(obs)
}

case class Poisson(mu: Double)(implicit N: Int) extends Dist[Int] {
  lazy val particles = unweighted(bdist.Poisson(mu).sample(N).toVector).particles
  def ll(obs: Int) = bdist.Poisson(mu).logProbabilityOf(obs)
}
```
Note that I've parameterised the Normal and Gamma the way that statisticians usually do, and not the way they are usually parametrised in scientific computing libraries (such as Breeze).

That's it! This is a complete, general-purpose, composible, monadic PPL. As presented, it's 52 lines of code. But that's 50 if we skip the 2 imports, or `cond`, or `fitQ`, or a standard distribution, or indeed put a couple of trailing `}` on the previous line. Let's now see how it works in practice.

## Examples

### Normal random sample

We'll start off with just about the simplest slightly interesting example I can think of: Bayesian inference for the mean and variance of a normal distribution from a random sample.
```tut:book
import breeze.stats.{meanAndVariance => meanVar}

val mod = for {
  mu <- Normal(0, 100)
  tau <- Gamma(1, 0.1)
  _ <- Normal(mu, 1.0/tau).fitQ(List(8.0,9,7,7,8,10))
} yield (mu,tau)
val modEmp = mod.empirical
meanVar(modEmp map (_._1)) // mu
meanVar(modEmp map (_._2)) // tau
```
So, the point is that the syntactic sugar for monadic binds (`flatMaps`) provided by Scala's for-expressions (similar to do-notation in Haskell) leads to readable code not so different to that in well-known general-purpose PPLs such as BUGS, JAGS, or Stan. There are some important differences, however. In particular, the embedded DSL has probabilistic programs as regular values in the host language. These may be manipulated and composed like other values. This makes this probabilistic programming language more composable than the aforementioned languages, which makes it much simpler to build large, complex probabilistic programs from simpler, well-tested, components, in a scalable way. That is, this PPL we have obtained "for free" is actually in many ways *better* than most well-known PPLs.

### Linear model

Because our PPL is embedded, we can take full advantage of the power of the host programming language to build our models. Let's explore this in the context of Bayesian estimation of a linear model.

```tut:book
val x = List(1.0,2,3,4,5,6)
val y = List(3.0,2,4,5,5,6)
val xy = x zip y
case class Param(alpha: Double, beta: Double, v: Double)
val prior = for {
  alpha <- Normal(0,10)
  beta <- Normal(0,4)
  v <- Gamma(1,0.1)
} yield Param(alpha, beta, v)
def addPoint(current: Prob[Param], obs: (Double, Double)): Prob[Param] = for {
    p <- current
    (x, y) = obs
    _ <- Normal(p.alpha + p.beta * x, p.v).fitQ(y)
  } yield p
val mod = xy.foldLeft(prior)(addPoint(_,_)).empirical
meanVar(mod map (_.alpha))
meanVar(mod map (_.beta))
meanVar(mod map (_.v))
```

### Dynamic generalised linear model

## Summary, conclusions, and further reading

