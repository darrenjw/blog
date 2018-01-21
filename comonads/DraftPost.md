# Comonads for scientific and statistical computing in Scala

*This is a work-in-progress draft - final post will be posted on my wordpress.com blog*

## Introduction

In a [previous post](https://darrenjw.wordpress.com/2016/04/15/first-steps-with-monads-in-scala/) I've given a brief introduction to *monads* in Scala, aimed at people interested in scientific and statistical computing. Monads are a concept from category theory which turn out to be exceptionally useful for solving many problems in functional programming. But most categorical concepts have a *dual*, usually prefixed with "co", so the dual of a monad is the *comonad*. Comonads turn out to be especially useful for formulating algorithms from scientific and statistical computing in an elegant way. In this post I'll illustrate their use in signal processing, image processing, numerical integration of PDEs, and Gibbs sampling (of an Ising model). Comonads enable the *extension* of a *local computation* to a *global computation*, and this pattern crops up all over the place in statistical computing.

## Monads and comonads

Simplifying massively, from the viewpoint of a Scala programmer, a monad is a mappable (functor) typeclass augmented with the methods `pure` and `flatMap`:
```scala
trait Monad[M[_]] extends Functor[M] {
  def pure[T](v: T): M[T]
  def flatMap[T,S](v: M[T])(f: T => M[S]): M[S]
}
```
In category theory, the dual of a concept is typically obtained by "reversing the arrows". Here that means reversing the direction of the methods `pure` and `flatMap` to get `extract` and `coflatMap`, respectively.
```scala
trait Comonad[W[_]] extends Functor[W] {
  def extract[T](v: W[T]): T
  def coflatMap[T,S](v: W[T])(f: W[T] => S): W[S]
}
```
So, while `pure` allows you to wrap plain values in a monad, `extract` allows you to get a value out of a comonad. So you can always get a value out of a comonad (unlike a monad). Similarly, while `flatMap` allows you to transform a monad using a function returning a monad, `coflatMap` allows you to transform a comonad using a function which collapses a comonad to a single value. It is `coflatMap` (sometimes called `extend`) which can extend a local computation (producing a single value) to the entire comonad. We'll look at how that works in the context of some familiar examples.

## Applying a linear filter to a data stream

One of the simplest examples of a comonad is an *infinite* stream of data. I've discussed streams in a [previous post](https://darrenjw.wordpress.com/2017/04/01/mcmc-as-a-stream/). By focussing on infinite streams we know the stream will never be empty, so there will always be a value that we can `extract`. Which value does `extract` give? For a `Stream` encoded as some kind of lazy list, the only value we actually know is the value at the head of the stream, with subsequent values to be lazily computed as required. So the head of the list is the only reasonable value for `extract` to return.

Understanding `coflatMap` is a bit more tricky, but it is `coflatMap` that provides us with the power to apply a non-trivial statistical computation to the stream. The input is a function which transforms a stream into a value. In our example, that will be a function which computes a weighted average of the first few values and returns that weighted average as the result. But the return type of `coflatMap` must be a stream of such computations. Following the types, a few minutes thought reveals that the only reasonable thing to do is to return the stream formed by applying the weighted average function to all sub-streams, recursively. So, for a `Stream` `s` (of type `Stream[T]`) and an input function `f: W[T] => S`, we form a stream whose head is `f(s)` and whose tail is `coflatMap(f)` applied to `s.tail`. Again, since we are working with an infinite stream, we don't have to worry about whether or not the `tail` is empty. This gives us our comonaidic `Stream`, and it is exactly what we need for applying a linear filter to the data stream.

In Scala, [Cats](https://typelevel.org/cats/) is a library providing typeclasses from Category theory, and instances of those typeclasses for parameterised types in the standard library. In particular, it provides us with comonadic functionality for the standard Scala `Stream`. Let's start by defining a stream corresponding to the [logistic map](https://en.wikipedia.org/wiki/Logistic_map).
```scala
import cats._
import cats.implicits._

val lam = 3.7
def s = Stream.iterate(0.5)(x => lam*x*(1-x))
s.take(10).toList
// res0: List[Double] = List(0.5, 0.925, 0.25668749999999985,
//  0.7059564011718747, 0.7680532550204203, 0.6591455741499428, ...
```
Let us now suppose that we want to apply a [linear filter](https://en.wikipedia.org/wiki/Linear_filter) to this stream, in order to smooth the values. The idea behind using comonads is that you figure out how to generate *one* desired value, and let `coflatMap` take care of applying the same logic to the rest of the structure. So here, we need a function to generate the *first* filtered value (since `extract` is focussed on the head of the stream). A simple first attempt a function to do this might look like the following.
```scala
  def linearFilterS(weights: Stream[Double])(s: Stream[Double]): Double =
    (weights, s).parMapN(_*_).sum
```
This aligns each weight in parallel with a corresponding value from the stream, and combines them using multiplication. The resulting finite stream is then summed (with addition).	We can test this with
```scala
linearFilterS(Stream(0.25,0.5,0.25))(s)
// res1: Double = 0.651671875
```
and let `coflatMap` extend this computation to the rest of the stream with something like:
```scala
s.coflatMap(linearFilterS(Stream(0.25,0.5,0.25))).take(5).toList
// res2: List[Double] = List(0.651671875, 0.5360828502929686, ...
```

This is all completely fine, but our `linearFilterS` function is specific to the `Stream` comonad, despite the fact that all we've used about it in the function is that it is a parallelly composable and foldable. We can make this much more generic as follows:
```scala
  def linearFilter[F[_]: Foldable, G[_]](
    weights: F[Double], s: F[Double]
  )(implicit ev: NonEmptyParallel[F, G]): Double =
    (weights, s).parMapN(_*_).fold
```
This uses some fairly advanced Scala concepts which I don't want to get into right now (I should also acknowledge that I had trouble getting the syntax right for this, and got help from Fabio Labella (@SystemFw) on the [Cats gitter channel](https://gitter.im/typelevel/cats)). But this version is more generic, and can be used to linearly filter other data structures than `Stream`. We can use this for regular `Streams` as follows:
```scala
s.coflatMap(s => linearFilter(Stream(0.25,0.5,0.25),s))
// res3: scala.collection.immutable.Stream[Double] = Stream(0.651671875, ?)
```
But we can apply this new filter to other collections. This could be other, more sophisticated, streams such as provided by [FS2](https://github.com/functional-streams-for-scala/fs2/), [Monix](https://monix.io/) or [Akka streams](https://doc.akka.io/docs/akka/current/stream/index.html?language=scala). But it could also be a non-stream collection, such as `List`:
```scala
val sl = s.take(10).toList
sl.coflatMap(sl => linearFilter(List(0.25,0.5,0.25),sl))
// res4: List[Double] = List(0.651671875, 0.5360828502929686, ...
```
Assuming that we have the [Breeze](https://darrenjw.wordpress.com/2013/12/30/brief-introduction-to-scala-and-breeze-for-statistical-computing/) scientific library available, we can plot the raw and smoothed trajectories.
```scala
def myFilter(s: Stream[Double]): Double =
  linearFilter(Stream(0.25, 0.5, 0.25),s)
val n = 500
import breeze.plot._
import breeze.linalg._
val fig = Figure(s"The (smoothed) logistic map (lambda=$lam)")
val p0 = fig.subplot(3,1,0)
p0 += plot(linspace(1,n,n),s.take(n))
p0.ylim = (0.0,1.0)
p0.title = s"The logistic map (lambda=$lam)"
val p1 = fig.subplot(3,1,1)
p1 += plot(linspace(1,n,n),s.coflatMap(myFilter).take(n))
p1.ylim = (0.0,1.0)
p1.title = "Smoothed by a simple linear filter"
val p2 = fig.subplot(3,1,2)
p2 += plot(linspace(1,n,n),s.coflatMap(myFilter).coflatMap(myFilter).coflatMap(myFilter).coflatMap(myFilter).coflatMap(myFilter).take(n))
p2.ylim = (0.0,1.0)
p2.title = "Smoothed with 5 applications of the linear filter"
fig.refresh
```
![Logistic map plots](logmap.png)

## Image processing and the heat equation

Streaming data is no way the only context in which a comonadic approach facilitates an elegant approach to scientific and statistical computing. Comonads crop up anywhere where we want to extend a computation that is local to a small part of a data structure to the full data structure. 

MORE!

I should acknowledge that this section of the post is very much influenced by a blog post on [comonadic image processing](https://jaspervdj.be/posts/2014-11-27-comonads-image-processing.html) in Haskell.

MORE!

## Gibbs sampling the Ising model

Another place where the concept of extending a local computation to a global computation crops up is in the context of [Gibbs sampling](https://en.wikipedia.org/wiki/Gibbs_sampling) a high-dimensional probability distribution by cycling through the sampling of each variable in turn from its full-conditional distribution. I'll illustrate this here using the [Ising model](https://en.wikipedia.org/wiki/Ising_model), so that I can reuse the pointed image class from above, but the principles apply to any Gibbs sampling problem.

MORE!

## Further reading

There are quite a few blog posts discussing comonads in the context of Haskell. In particular, the post on comonads for [image analysis](https://jaspervdj.be/posts/2014-11-27-comonads-image-processing.html) I mentioned previously, and [this one](http://blog.sigfpe.com/2006/12/evaluating-cellular-automata-is.html) on cellular automata. Bartosz's post on [comonads](https://bartoszmilewski.com/2017/01/02/comonads/) gives some connection back to the mathematical origins. Runar's [Scala comonad tutorial](http://blog.higher-order.com/blog/2015/06/22/a-scala-comonad-tutorial/) is the best source I know for comonads in Scala.

Full runnable code corresponding to this blog post is available from my [blog repo](https://github.com/darrenjw/blog/tree/master/comonads).

#### eof

