# MCMC as a Stream

## Rough draft of post in advance of publishing on WordPress

### Introduction

This weekend I've been preparing some material for my upcoming [Scala for statistical computing](https://github.com/darrenjw/scala-course/blob/master/README.md) short course. As part of the course, I thought it would be useful to walk through how to think about and structure MCMC codes, and in particular, how to think about MCMC algorithms as infinite streams of state. This material is reasonably stand-alone, so it seems suitable for a blog post. Complete runnable code for the examples in this post are available from my [blog repo](https://github.com/darrenjw/blog/tree/master/mcmc-stream).

### A simple MH sampler

For this post I will just consider a trivial toy Metropolis algorithm using a Uniform random walk proposal to target a standard normal distribution. I've considered this problem before on my blog, so if you aren't very familiar with Metropolis-Hastings algorithms, you might want to quickly review my post on [Metropolis-Hastings MCMC algorithms in R](https://darrenjw.wordpress.com/2010/08/15/metropolis-hastings-mcmc-algorithms/) before continuing. At the end of that post, I gave the following R code for the Metropolis sampler:
```r
metrop3<-function(n=1000,eps=0.5) 
{
        vec=vector("numeric", n)
        x=0
        oldll=dnorm(x,log=TRUE)
        vec[1]=x
        for (i in 2:n) {
                can=x+runif(1,-eps,eps)
                loglik=dnorm(can,log=TRUE)
                loga=loglik-oldll
                if (log(runif(1)) < loga) { 
                        x=can
                        oldll=loglik
                        }
                vec[i]=x
        }
        vec
}
```
I will begin this post with a fairly direct translation of this algorithm into Scala:
```scala
  def metrop1(n: Int = 1000, eps: Double = 0.5): DenseVector[Double] = {
    val vec = DenseVector.fill(n)(0.0)
    var x = 0.0
    var oldll = Gaussian(0.0, 1.0).logPdf(x)
    vec(0) = x
    (1 until n).foreach { i =>
      val can = x + Uniform(-eps, eps).draw
      val loglik = Gaussian(0.0, 1.0).logPdf(can)
      val loga = loglik - oldll
      if (math.log(Uniform(0.0, 1.0).draw) < loga) {
        x = can
        oldll = loglik
      }
      vec(i) = x
    }
    vec
}
```
This code works, and is reasonably fast and efficient, but there are several issues with it from a functional programmers perspective. One issue is that we have committed to storing all MCMC output in RAM in a `DenseVector`. This probably isn't an issue here, but for some big problems we might prefer to not store the full set of states, but to just print the states to (say) the console, for possible re-direction to a file. It is easy enough to modify the code to do this:
```scala
def metrop2(n: Int = 1000, eps: Double = 0.5): Unit = {
    var x = 0.0
    var oldll = Gaussian(0.0, 1.0).logPdf(x)
    (1 to n).foreach { i =>
      val can = x + Uniform(-eps, eps).draw
      val loglik = Gaussian(0.0, 1.0).logPdf(can)
      val loga = loglik - oldll
      if (math.log(Uniform(0.0, 1.0).draw) < loga) {
        x = can
        oldll = loglik
      }
      println(x)
    }
}
```
But now we have two version of the algorithm. One for storing results locally, and one for streaming results to the console. This is clearly unsatisfactory, but we shall return to this issue shortly. Another issue that will jump out at functional programmers is the reliance on mutable variables for storing the state and old likelihood. Let's fix that now by re-writing the algorithm as a tail-recursion.
```scala
@tailrec
def metrop3(n: Int = 1000, eps: Double = 0.5, x: Double = 0.0, oldll: Double = Double.MinValue): Unit = {
    if (n > 0) {
      println(x)
      val can = x + Uniform(-eps, eps).draw
      val loglik = Gaussian(0.0, 1.0).logPdf(can)
      val loga = loglik - oldll
      if (math.log(Uniform(0.0, 1.0).draw) < loga)
        metrop3(n - 1, eps, can, loglik)
      else
        metrop3(n - 1, eps, x, oldll)
    }
  }
```
This has eliminated the `var`s, and is just as fast and efficient as the previous version of the code. Note that the `@tailrec` annotation is optional - it just signals to the compiler that we want it to throw an error if for some reason it cannot eliminate the tail call. However, this is for the print-to-console version of the code. What if we actually want to keep the iterations in RAM for subsequent analysis? We can keep the values in an accumulator, as follows.
```scala
@tailrec
def metrop4(n: Int = 1000, eps: Double = 0.5, x: Double = 0.0, oldll: Double = Double.MinValue, acc: List[Double] = Nil): DenseVector[Double] = {
    if (n == 0)
      DenseVector(acc.reverse.toArray)
    else {
      val can = x + Uniform(-eps, eps).draw
      val loglik = Gaussian(0.0, 1.0).logPdf(can)
      val loga = loglik - oldll
      if (math.log(Uniform(0.0, 1.0).draw) < loga)
        metrop4(n - 1, eps, can, loglik, can :: acc)
      else
        metrop4(n - 1, eps, x, oldll, x :: acc)
    }
}
```

### Factoring out the updating logic

This is all fine, but we haven't yet addressed the issue of having different versions of the code depending on what we want to do with the output. The problem is that we have tied up the logic of advancing the Markov chain with what to do with the output. What we need to do is separate out the code for advancing the state. We can do this by defining a new function.
```scala
def newState(x: Double, oldll: Double, eps: Double): (Double, Double) = {
    val can = x + Uniform(-eps, eps).draw
    val loglik = Gaussian(0.0, 1.0).logPdf(can)
    val loga = loglik - oldll
    if (math.log(Uniform(0.0, 1.0).draw) < loga) (can, loglik) else (x, oldll)
}
```
This function takes as input a current state and associated log likelihood and returns a new state and log likelihood following the execution of one step of a MH algorithm. This separates the concern of state updating from the rest of the code. So now if we want to write code that prints the state, we can write it as
```scala
  @tailrec
  def metrop5(n: Int = 1000, eps: Double = 0.5, x: Double = 0.0, oldll: Double = Double.MinValue): Unit = {
    if (n > 0) {
      println(x)
      val ns = newState(x, oldll, eps)
      metrop5(n - 1, eps, ns._1, ns._2)
    }
  }
```
and if we want to accumulate the set of states visited, we can write that as
```scala
  @tailrec
  def metrop6(n: Int = 1000, eps: Double = 0.5, x: Double = 0.0, oldll: Double = Double.MinValue, acc: List[Double] = Nil): DenseVector[Double] = {
    if (n == 0) DenseVector(acc.reverse.toArray) else {
      val ns = newState(x, oldll, eps)
      metrop6(n - 1, eps, ns._1, ns._2, ns._1 :: acc)
    }
  }
```
Both of these functions call `newState` to do the real work, and concentrate on what to do with the sequence of states. However, both of these functions repeat the logic of how to iterate over the sequence of states.

### MCMC as a stream

Ideally we would like to abstract out the details of how to do state iteration from the code as well. Most functional languages have some concept of a `Stream`, which represents a (potentially infinite) sequence of states. The `Stream` can embody the logic of how to perform state iteration, allowing us to abstract that away from our code, as well.

To do this, we will restructure our code slightly so that it more clearly maps old state to new state.
```scala
def nextState(eps: Double)(state: (Double, Double)): (Double, Double) = {
    val x = state._1
    val oldll = state._2
    val can = x + Uniform(-eps, eps).draw
    val loglik = Gaussian(0.0, 1.0).logPdf(can)
    val loga = loglik - oldll
    if (math.log(Uniform(0.0, 1.0).draw) < loga) (can, loglik) else (x, oldll)
}
```
The "real" state of the chain is just `x`, but if we want to avoid recalculation of the old likelihood, then we need to make this part of the chain's state. We can use this `nextState` function in order to construct a `Stream`.
```scala
  def metrop7(eps: Double = 0.5, x: Double = 0.0, oldll: Double = Double.MinValue): Stream[Double] =
    Stream.iterate((x, oldll))(nextState(eps)) map (_._1)
```
The result of calling this is an infinite stream of states. Obviously it isn't computed - that would require infinite computation, but it captures the logic of iteration and computation in a `Stream`, that can be thought of as a lazy `List`. We can get values out by converting the `Stream` to a regular collection, being careful to truncate the `Stream` to one of finite length beforehand! eg. `metrop7().drop(1000).take(10000).toArray` will do a burn-in of 1,000 iterations followed by a main monitoring run of length 10,000, capturing the results in an `Array`. Note that `metrop7().drop(1000).take(10000)` is a `Stream`, and so nothing is actually computed until the `toArray` is encountered. Conversely, if printing to console is required, just replace the `.toArray` with `.foreach(println)`.

The above stream-based approach to MCMC iteration is clean and elegant, and deals nicely with issues like burn-in and thinning (which can be handled similarly). This is how I typically write MCMC codes these days. However, functional programming purists would still have issues with this approach, as it isn't quite pure functional. The problem is that the code isn't pure - it has a side-effect, which is to mutate the state of the under-pinning pseudo-random number generator. If the code was pure, calling `nextState` with the same inputs would always give the same result. Clearly this isn't the case here, as we have specifically designed the function to be stochastic, returning a randomly sampled value from the desired probability distribution. So `nextState` represents a function for randomly sampling from a conditional probability distribution.

### A pure functional approach

Now, ultimately all code has side-effects, or there would be no point in running it! But in functional programming the desire is to make as much of the code as possible pure, and to push side-effects to the very edges of the code. So it's fine to have side-effects in your `main` method, but not buried deep in your code. Here the side-effect is at the very heart of the code, which is why it is potentially an issue.

To keep things as simple as possible, at this point we will stop worrying about carrying forward the old likelihood, and hard-code a value of `eps`. Generalisation is straightforward.
We can make our code pure by instead defining a function which represents the conditional probability distribution itself. For this we use a *probability monad*, which in [Breeze](https://github.com/scalanlp/breeze/) is called `Rand`. We can couple together such functions using monadic binds (`flatMap` in Scala), expressed most neatly using for-comprehensions. So we can write our transition kernel as
```scala
def kernel(x: Double): Rand[Double] = for {
    innov <- Uniform(-0.5, 0.5)
    can = x + innov
    oldll = Gaussian(0.0, 1.0).logPdf(x)
    loglik = Gaussian(0.0, 1.0).logPdf(can)
    loga = loglik - oldll
    u <- Uniform(0.0, 1.0)
} yield if (math.log(u) < loga) can else x
```
This is now pure - the same input `x` will always return the same probability distribution - the conditional distribution of the next state given the current state. We can draw random samples from this distribution if we must, but it's probably better to work as long as possible with pure functions. So next we need to encapsulate the iteration logic. Breeze has a `MarkovChain` object which can take kernels of this form and return a stochastic `Process` object representing the iteration logic, as follows.
```scala
MarkovChain(0.0)(kernel).
  steps.
  drop(1000).
  take(10000).
  foreach(println)
```
The `steps` method contains the logic of how to advance the state of the chain. But again note that no computation actually takes place until the `foreach` method is encountered - this is when the sampling occurs and the side-effects happen.

Metropolis-Hastings is a common use-case for Markov chains, so Breeze actually has a helper method built-in that will construct a MH sampler directly from an initial state, a proposal kernel, and a (log) target.
```scala
MarkovChain.
  metropolisHastings(0.0, (x: Double) =>
  Uniform(x - 0.5, x + 0.5))(x =>
  Gaussian(0.0, 1.0).logPdf(x)).
  steps.
  drop(1000)
  take(10000).
  toArray
```
Note that if you are using the MH functionality in Breeze, it is important to make sure that you are using version 0.13 (or later), as I fixed a few issues with the MH code shortly prior to the 0.13 release.

### Summary

Viewing MCMC algorithms as infinite streams of state is useful for writing elegant, generic, flexible code. Streams occur everywhere in programming, and so there are lots of libraries for working with them. In this post I used the simple `Stream` from the Scala standard library, but there are much more powerful and flexible stream libraries for Scala, including [fs2](https://github.com/functional-streams-for-scala/fs2) and [Akka-streams](http://doc.akka.io/docs/akka/2.4/scala/stream/index.html). But whatever libraries you are using, the fundamental concepts are the same. The most straightforward approach to implementation is to define impure stochastic streams to consume. However, a pure functional approach is also possible, and the Breeze library defines some useful functions to facilitate this approach. I'm still a little bit ambivalent about whether the pure approach is worth the additional cognitive overhead, but it's certainly very interesting and worth playing with and thinking about the pros and cons.

Complete runnable code for the examples in this post are available from my [blog repo](https://github.com/darrenjw/blog/tree/master/mcmc-stream).


### eof


