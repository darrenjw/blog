# A probability monad for the bootstrap particle filter

## Introduction

In the previous post I showed how to [write your own general-purpose monadic probabilistic programming language from scratch in 50 lines of (Scala) code](https://darrenjw.wordpress.com/2019/08/07/write-your-own-general-purpose-monadic-probabilistic-programming-language-from-scratch-in-50-lines-of-scala-code/). That post is a pre-requisite for this one, so if you haven't read it, go back and have a quick skim through it before proceeding. In that post I tried to keep everything as simple as possible, but at the expense of both elegance and efficiency. In this post I'll address one problem with the implementation from that post - the memory (and computational) overhead associated with forming the Cartesian product of particle sets during monadic binding (`flatMap`). So if particle sets are typically of size $N$, then the Cartesian product is of size $N^2$, and multinomial resampling is applied to this set of size $N^2$ in order to sample back down to a set of size $N$. But this isn't actually necessary. We can directly construct a set of size $N$, certainly saving memory, but also potentially saving computation time if the conditional distribution (on the right of the monadic bind) can be efficiently sampled. If we do this we will have a probability monad encapsulating the logic of a bootstrap [particle filter](https://en.wikipedia.org/wiki/Particle_filter), such as is often used for computing the filtering distribution of a state-space model in time series analysis. This simple change won't solve the computational issues associated with deep monadic binding, but does solve the memory problem, and can lead to computationally efficient algorithms so long as care is taken in the formulation of probabilistic programs to ensure that deep monadic binding doesn't occur. We'll discuss that issue in the context of state-space models later, once we have our new SMC-based probability monad.

Materials for this post can be found in my [blog repo](https://github.com/darrenjw/blog/tree/master/min-ppl2), and a draft of this post itself can be found in the form of an executable [tut document](https://github.com/darrenjw/blog/blob/master/min-ppl2/src/main/tut/DraftPost.md). 

## An SMC-based monad

The idea behind the approach to binding used in this filter is to mimic the "predict" step of a bootstrap [particle filter](https://en.wikipedia.org/wiki/Particle_filter). Here, for each particle in the source distribution, exactly *one* particle is drawn from the required conditional distribution and paired with the source particle, preserving the source particle's original weight. So, in order to operationalise this, we will need a `draw` method adding into our probability monad. It will also simplify things to add a `flatMap` method to our `Particle` type constructor.

To follow along, you can type `sbt console` from the `min-ppl2` directory of my blog repo, then paste blocks of code one at a time.
```tut:silent
  import breeze.stats.{distributions => bdist}
  import breeze.linalg.DenseVector
  import cats._
  import cats.implicits._

  implicit val numParticles = 2000

  case class Particle[T](v: T, lw: Double) { // value and log-weight
    def map[S](f: T => S): Particle[S] = Particle(f(v), lw)
    def flatMap[S](f: T => Particle[S]): Particle[S] = {
      val ps = f(v)
      Particle(ps.v, lw + ps.lw)
    }
  }
```
I've added a dependence on [cats](https://typelevel.org/cats/) here, so that we can use some derived methods, later. To take advantage of this, we must provide evidence that our custom types conform to standard type class interfaces. For example, we can provide evidence that `Particle[_]` is a monad as follows.
```tut:silent
  implicit val particleMonad = new Monad[Particle] {
    def pure[T](t: T): Particle[T] = Particle(t, 0.0)
    def flatMap[T,S](pt: Particle[T])(f: T => Particle[S]): Particle[S] = pt.flatMap(f)
    def tailRecM[T,S](t: T)(f: T => Particle[Either[T,S]]): Particle[S] = ???
  }
```
The technical details are not important for this post, but we'll see later what this can give us.

We can now define our `Prob[_]` monad in the following way.
```tut:silent
object Wrapped {
  trait Prob[T] {
    val particles: Vector[Particle[T]]
    def draw: Particle[T]
    def mapP[S](f: T => Particle[S]): Prob[S] = Empirical(particles map (_ flatMap f))
    def map[S](f: T => S): Prob[S] = mapP(v => Particle(f(v), 0.0))
    def flatMap[S](f: T => Prob[S]): Prob[S] = mapP(f(_).draw)
    def resample(implicit N: Int): Prob[T] = {
      val lw = particles map (_.lw)
      val mx = lw reduce (math.max(_,_))
      val rw = lw map (lwi => math.exp(lwi - mx))
      val law = mx + math.log(rw.sum/(rw.length))
      val ind = bdist.Multinomial(DenseVector(rw.toArray)).sample(N)
      val newParticles = ind map (i => particles(i))
      Empirical(newParticles.toVector map (pi => Particle(pi.v, law)))
    }
    def cond(ll: T => Double): Prob[T] = mapP(v => Particle(v, ll(v)))
    def empirical: Vector[T] = resample.particles.map(_.v)
  }

  case class Empirical[T](particles: Vector[Particle[T]]) extends Prob[T] {
    def draw: Particle[T] = {
      val lw = particles map (_.lw)
      val mx = lw reduce (math.max(_,_))
      val rw = lw map (lwi => math.exp(lwi - mx))
      val law = mx + math.log(rw.sum/(rw.length))
      val idx = bdist.Multinomial(DenseVector(rw.toArray)).draw
      Particle(particles(idx).v, law)
    }
  }
  
}
import Wrapped._
```
As before, if you are pasting code blocks into the REPL, you will need to use `paste:` mode to paste these two definitions together.

The essential structure is similar to that from the previous post, but with a few notable differences. Most fundamentally, we now require any concrete implementation to provide a `draw` method returning a single particle from the distribution. Like before, we are not worrying about purity of functional code here, and using a standard random number generator with a globally mutating state. We can define a `mapP` method (for "map particle") using the new `flatMap` method on `Particle`, and then use that to define `map` and `flatMap` for `Prob[_]`. Crucially, `draw` is used to define `flatMap` without requiring a Cartesian product of distributions to be formed.

We add a `draw` method to our `Empirical[_]` implementation. This method is computationally intensive, so it will still be computationally problematic to chain several `flatMap`s together, but this will no longer be $N^2$ in memory utilisation. Note that again we carefully set the weight of the drawn particle so that its raw weight is the average of the raw weight of the empirical distribution. This is needed to propagate conditioning information correctly back through `flatMaps`. There is obviously some code duplication between the `draw` method on `Empirical` and the `resample` method on `Prob`, but I'm not sure it's worth factoring out.

It is worth noting that neither `flatMap` nor `cond` triggers resampling, so the user of the library is now responsible for resampling when appropriate.

We can provide evidence that `Prob[_]` forms a monad just like we did `Particle[_]`.
```tut:silent
  implicit val probMonad = new Monad[Prob] {
    def pure[T](t: T): Prob[T] = Empirical(Vector(Particle(t, 0.0)))
    def flatMap[T,S](pt: Prob[T])(f: T => Prob[S]): Prob[S] = pt.flatMap(f)
    def tailRecM[T,S](t: T)(f: T => Prob[Either[T,S]]): Prob[S] = ???
  }
```
Again, we'll want to be able to create a distribution from an unweighted collection of values.
```tut:silent
  def unweighted[T](ts: Vector[T], lw: Double = 0.0): Prob[T] =
    Empirical(ts map (Particle(_, lw)))
```
We will again define an implementation for distributions with tractable likelihoods, which are therefore easy to condition on. They will typically also be easy to draw from efficiently, and we will use this fact, too.
```tut:silent
  trait Dist[T] extends Prob[T] {
    def ll(obs: T): Double
    def ll(obs: Seq[T]): Double = obs map (ll) reduce (_+_)
    def fit(obs: Seq[T]): Prob[T] = mapP(v => Particle(v, ll(obs)))
    def fitQ(obs: Seq[T]): Prob[T] = Empirical(Vector(Particle(obs.head, ll(obs))))
    def fit(obs: T): Prob[T] = fit(List(obs))
    def fitQ(obs: T): Prob[T] = fitQ(List(obs))
  }
```
We can give implementations of this for a few standard distributions.
```tut:silent
  case class Normal(mu: Double, v: Double)(implicit N: Int) extends Dist[Double] {
    lazy val particles = unweighted(bdist.Gaussian(mu, math.sqrt(v)).
      sample(N).toVector).particles
    def draw = Particle(bdist.Gaussian(mu, math.sqrt(v)).draw, 0.0)
    def ll(obs: Double) = bdist.Gaussian(mu, math.sqrt(v)).logPdf(obs)
  }

  case class Gamma(a: Double, b: Double)(implicit N: Int) extends Dist[Double] {
    lazy val particles = unweighted(bdist.Gamma(a, 1.0/b).
      sample(N).toVector).particles
    def draw = Particle(bdist.Gamma(a, 1.0/b).draw, 0.0)
    def ll(obs: Double) = bdist.Gamma(a, 1.0/b).logPdf(obs)
  }

  case class Poisson(mu: Double)(implicit N: Int) extends Dist[Int] {
    lazy val particles = unweighted(bdist.Poisson(mu).
      sample(N).toVector).particles
    def draw = Particle(bdist.Poisson(mu).draw, 0.0)
    def ll(obs: Int) = bdist.Poisson(mu).logProbabilityOf(obs)
  }
```
Note that we now have to provide an (efficient) `draw` method for each implementation, returning a single draw from the distribution as a `Particle` with a (raw) weight of 1 (log weight of 0).

We are done. It's a few more lines of code than that from the previous post, but this is now much closer to something that could be used in practice to solve actual inference problems using a reasonable number of particles. But to do so we will need to be careful do avoid deep monadic binding. This is easiest to explain with a concrete example.

## Using the SMC-based probability monad in practice

### Monadic binding and applicative structure

As explained in the previous post, using Scala's `for`-expressions for monadic binding gives a natural and elegant PPL for our probability monad "for free". This is fine, and in general there is no reason why using it should lead to inefficient code. However, for this particular probability monad implementation, it turns out that deep monadic binding comes with a huge performance penalty. For a concrete example, consider the following specification, perhaps of a prior distribution over some independent parameters.
```scala
    val prior = for {
      x <- Normal(0,1)
      y <- Gamma(1,1)
      z <- Poisson(10)
    } yield (x,y,z)
```
Don't paste that into the REPL - it will take an age to complete!

Again, I must emphasise that there is nothing *wrong* with this specification, and there is no reason in principle why such a specification can't be computationally efficient - it's just a problem for our particular probability monad. We can begin to understand the problem by thinking about how this will be de-sugared by the compiler. Roughly speaking, the above will de-sugar to the following nested `flatMap`s.
```scala
    val prior2 =
      Normal(0,1) flatMap {x =>
        Gamma(1,1) flatMap {y =>
          Poisson(10) map {z =>
            (x,y,z)}}}
```
Again, beware of pasting this into the REPL.

So, although written from top to bottom, the nesting is such that the `flatMap`s collapse from the bottom-up. The second `flatMap` (the first to collapse) isn't such a problem here, as the `Poisson` has a $O(1)$ `draw` method. But the result is an empirical distribution, which has an $O(N)$ draw method. So the first `flatMap` (the second to collapse) is an $O(N^2)$ operation. By extension, it's easy to see that the computational cost of nested `flatMap`s will be *exponential* in the number of monadic binds. So, looking back at the `for` expression, the problem is that there are three `<-`. The last one isn't a problem since it corresponds to a `map`, and the second last one isn't a problem, since the final distribution is tractable with an $O(1)$ `draw` method. The problem is the first `<-`, corresponding to a `flatMap` of one empirical distribution with respect to another. For our particular probability monad, it's best to avoid these if possible.

The interesting thing to note here is that because the distributions are independent, there is no need for them to be sequenced. They could be defined in any order. In this case it makes sense to use the applicative structure implied by the monad. 

Now, since we have told [cats](https://typelevel.org/cats/) that `Prob[_]` is a monad, it can provide appropriate applicative methods for us automatically. In Cats, every monad is assumed to be also an applicative functor (which is true in [Cartesian closed categories](https://en.wikipedia.org/wiki/Cartesian_closed_category), and Cats implicitly assumes that all functors and monads are defined over CCCs). So we can give an alternative specification of the above prior using applicative composition.
```tut:book
 val prior3 = Applicative[Prob].tuple3(Normal(0,1), Gamma(1,1), Poisson(10))
```
This one is mathematically equivalent, but safe to paste into your REPL, as it does not involve deep monadic binding, and can be used whenever we want to compose together independent components of a probabilistic program. Note that "tupling" is not the only possibility - Cats provides a range of functions for manipulating applicative values.

This is one way to avoid deep monadic binding, but another strategy is to just break up a large `for` expression into separate smaller `for` expressions. We can examine this strategy in the context of state-space modelling.

### Particle filtering for a non-linear state-space model

We can now re-visit the DGLM example from the previous post. We began by declaring some observations and a prior.
```tut:book
    val data = List(2,1,0,2,3,4,5,4,3,2,1)

    val prior = for {
      w <- Gamma(1, 1)
      state0 <- Normal(0.0, 2.0)
    } yield (w, List(state0))
```
Looking carefully at the `for`-expression, there are just two `<-`, and the distribution on the RHS of the `flatMap` is tractable, so this is just $O(N)$. So far so good.

Next, let's look at the function to add a time point, which previously looked something like the following.
```tut:book
    def addTimePointSIS(current: Prob[(Double, List[Double])],
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
```
Recall that our new probability monad does not automatically trigger resampling, so applying this function in a `fold` will lead to a simple sampling importance sampling (SIS) particle filter. Typically, the bootstrap particle filter includes resampling after each time point, giving a special case of a sampling importance resampling (SIR) particle filter, which we could instead write as follows.
```tut:book
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
```
This works fine, but we can see that there are three `<-` in this for expression. This leads to a `flatMap` with an empirical distribution on the RHS, and hence is $O(N^2)$. But this is simple enough to fix, by separating the updating process into separate "predict" and "update" steps, which is how people typically formulate particle filters for state-space models, anyway. Here we could write that as
```tut:book
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
```
By breaking the `for` expression into two: the first for the "predict" step and the second for the "update" (conditioning on the observation), we get two $O(N)$ operations, which for large $N$ is clearly much faster. We can then run the filter by folding over the observations.
```tut:book
  import breeze.stats.{meanAndVariance => meanVar}

  val mod = data.foldLeft(prior)(addTimePoint(_,_)).empirical

  meanVar(mod map (_._1)) // w
  meanVar(mod map (_._2.reverse.head)) // initial state
  meanVar(mod map (_._2.head)) // final state
```

## Summary and conclusions

Here we have just done some minor tidying up of the rather naive probability monad from the previous post to produce an SMC-based probability monad with improved performance characteristics. Again, we get an embedded probabilistic programming language "for free". Although the language itself is very flexible, allowing us to construct more-or-less arbitrary probabilistic programs for Bayesian inference problems, we saw that a bug/feature of this particular inference algorithm is that care must be taken to avoid deep monadic binding if reasonable performance is to be obtained. In most cases this is simple to achieve by using applicative composition or by breaking up large `for` expressions.

There are still many issues and inefficiencies associated with this PPL. In particular, if the main intended application is to state-space models, it would make more sense to tailor the algorithms and implementations to exactly that case. OTOH, if the main concern is a generic PPL, then it would make sense to make the PPL independent of the particular inference algorithm. These are both potential topics for future posts.


### Software

* [min-ppl2](https://github.com/darrenjw/blog/tree/master/min-ppl2) - code associated with this blog post
* [Rainier](https://github.com/stripe/rainier) - a more efficient PPL with similar syntax
* [monad-bayes](https://github.com/adscib/monad-bayes) - a Haskell library exploring related ideas



