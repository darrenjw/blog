# scala-smfsb tutorial

This tutorial document will walk through some basic features of the `scala-smfsb` library. Some familiarity with both Scala and the `smfsb` R package will be helpful, but is not strictly necessary. Note that the library relies on the Scala [Breeze](https://github.com/scalanlp/breeze/blob/master/README.md) library for linear algebra and probability distributions, so some familiarity with that library can also be helpful.

## Setup

To follow the tutorial, you need to have [Sbt](http://www.scala-sbt.org/) installed, and this in turn requires a recent [JDK](http://www.oracle.com/technetwork/java/javase/downloads). If you are new to Scala, you may find the [setup page](https://github.com/darrenjw/scala-course/blob/master/Setup.md) for my [Scala course](https://github.com/darrenjw/scala-course/blob/master/StartHere.md) to be useful, but note that on many Linux systems it can be as simple as installing the packages `openjdk-8-jdk` and `sbt`.

Once you have Sbt installed, you should be able to run it by entering `sbt` at your OS command line. You now need to use Sbt to create a Scala REPL with a dependency on the `scala-smfsb` library. There are many ways to do this, but if you are new to Scala, the simplest way is probably to start up Sbt from an _empty_ or temporary directory (which doesn't contain any Scala code), and then paste the following into the Sbt prompt:
```scala
set libraryDependencies += "com.github.darrenjw" %% "scala-smfsb" % "0.5"
set libraryDependencies += "org.scalanlp" %% "breeze-viz" % "0.13.2"
set scalaVersion := "2.12.6"
console
```
The first time you run this it will take a little while to download and cache various library dependcies. But everything is cached, so it should be much quicker in future. When it is finished, you should have a Scala REPL ready to enter Scala code.

## An introduction to `scala-smfsb`

It should be possible to type or copy-and-paste the commands below one-at-a-time into the Scala REPL. We need to start with a few imports.
```tut:silent
import smfsb._
import breeze.linalg._
import breeze.numerics._
```
We are now ready to go. 

### Simulating models

Let's begin by instantiating a Lotka-Volterra model, simulating a single realisation of the process, and then plotting it.
```tut:silent
// Simulate LV with Gillespie
val model = SpnModels.lv[IntState]()
val step = Step.gillespie(model)
val ts = Sim.ts(DenseVector(50, 100), 0.0, 20.0, 0.05, step)
Sim.plotTs(ts, "Gillespie simulation of LV model with default parameters")
```
When the model is instantiated, it can use default rate constants for the reactions. But these can be over-written. The library uses Breeze `DenseVectors` to represent parameter vectors. 
```tut:silent
// Simulate LV with non-default parameters
val model2 = SpnModels.lv[IntState](DenseVector(1.0, 0.006, 0.3))
val step2 = Step.gillespie(model2)
val ts2 = Sim.ts(DenseVector(50, 40), 0.0, 50.0, 0.1, step2)
Sim.plotTs(ts2, "Gillespie simulation of LV model with non-default parameters")
```
The library comes with a few other models. There's a Michaelis-Menten enzyme kinetics model:
```tut:silent
// Simulate other models with Gillespie
val stepMM = Step.gillespie(SpnModels.mm[IntState]())
val tsMM = Sim.ts(DenseVector(301,120,0,0), 0.0, 100.0, 0.5, stepMM)
Sim.plotTs(tsMM, "Gillespie simulation of the MM model")
```
and an immigration-death model
```tut:silent
val stepID = Step.gillespie(SpnModels.id[IntState]())
val tsID = Sim.ts(DenseVector(0), 0.0, 40.0, 0.1, stepID)
Sim.plotTs(tsID, "Gillespie simulation of the ID model")
```
and an auto-regulatory genetic network model.
```tut:silent
val stepAR = Step.gillespie(SpnModels.ar[IntState]())
val tsAR = Sim.ts(DenseVector(10, 0, 0, 0, 0), 0.0, 500.0, 0.5, stepAR)
Sim.plotTs(tsAR, "Gillespie simulation of the AR model")
```
If you know the book and/or the R package, these models should all be familiar. We don't have to simulate data on a regular time grid.
```tut:silent
// Simulate on an irregular time grid
val tsi = Sim.times(DenseVector(50,100), 0.0, List(0.0,2.0,5.0,10.0,20.0), step)
Sim.plotTs(tsi, "Simulation on an irregular time grid")
```
We also don't have to just sample one realisation. We can look at many realisations of the same transition kernel, and then use Breeze-viz to plot the results.
```tut:silent
// Simulate a sample
val samp = Sim.sample(1000, DenseVector(50,100), 0.0, 10.0, step)
import breeze.plot._
val fig = Figure("Marginal of transition kernel")
fig.subplot(1,2,0) += hist(DenseVector(samp.map(_.data(0)).toArray))
fig.subplot(1,2,1) += hist(DenseVector(samp.map(_.data(1)).toArray))
```
We are not restricted to exact stochastic simulation using the Gillespie algorithm. We can use an approximate Poisson time-stepping algorithm.
```tut:silent
// Simulate LV with other algorithms
val stepPts = Step.pts(model)
val tsPts = Sim.ts(DenseVector(50, 100), 0.0, 20.0, 0.05, stepPts)
Sim.plotTs(tsPts, "Poisson time-step simulation of the LV model")
```
Alternatively, we can instantiate the example models using a continuous state rather than a discrete state, and then simulate using algorithms based on continous approximations, such as Euler-Maruyama simulation of a chemical Langevin equation (CLE) approximation. 
```tut:silent
val stepCle = Step.cle(SpnModels.lv[DoubleState]())
val tsCle = Sim.ts(DenseVector(50.0, 100.0), 0.0, 20.0, 0.05, stepCle)
Sim.plotTs(tsCle, "Euler-Maruyama/CLE simulation of the LV model")
```
If we want to ignore noise temporarily, there's also a simple continuous deterministic Euler integrator built-in.
```tut:silent
val stepE = Step.euler(SpnModels.lv[DoubleState]())
val tsE = Sim.ts(DenseVector(50.0, 100.0), 0.0, 20.0, 0.05, stepE)
Sim.plotTs(tsE, "Continuous-deterministic Euler simulation of the LV model")
```

### Defining models

There are a few built-in models, as seen above. But quite quickly you are likely to want to define your own. This is also very straightforward, using the constructor of the `UnmarkedSpn` class. Let's just pretend that the Lotka-Volterra model is not included in the library, and think about how we could define it from scratch. The simplest approach would be something like the following.
```tut:silent
val mylv0 = UnmarkedSpn[IntState](
  List("x", "y"),
  DenseMatrix((1, 0), (1, 1), (0, 1)),
  DenseMatrix((2, 0), (0, 2), (0, 0)),
  (x, t) => {
    DenseVector(
      x.data(0) * 1.0, x.data(0) * x.data(1) * 0.005, x.data(1) * 0.6
    )}
)
```
We create a fully parametrised model (without an initial marking), but providing a list of species names, a *Pre* and *Post* matrix, and a hazard vector, which in general may be a function of both the state, `x` and the current time, `t`. Note that it should be OK to write, say, `x(0)`, rather than `x.data(0)`, but sometimes correct resolution of the indexing fails with `IntState` (but doesn't for `DoubleState`). We can test that this works.
```tut:silent
val ts0 = Sim.ts(DenseVector(50, 100), 0.0, 20.0, 0.05, Step.gillespie(mylv0))
Sim.plotTs(ts0, "Gillespie simulation of LV0")
```
One potential issue with this definition is that the rate constants within the hazard vector are hard-coded. We can easily get around that by creating a function (or method) that accepts a parameter vector (of some kind) and outputs a fully parameterised SPN.
```tut:silent
def lvparam(p: DenseVector[Double] = DenseVector(1.0, 0.005, 0.6)): Spn[IntState] =
  UnmarkedSpn[IntState](
    List("x", "y"),
    DenseMatrix((1, 0), (1, 1), (0, 1)),
    DenseMatrix((2, 0), (0, 2), (0, 0)),
    (x, t) => {
      DenseVector(
        x.data(0) * p(0), x.data(0) * x.data(1) * p(1), x.data(1) * p(2)
      )}
  )
```
Using a method allows the inclusion of a default parameter vector, which can be convenient. The follow code shows how we can use this.
```tut:silent
val mylv1 = lvparam(DenseVector(1.0, 0.005, 0.6))
val tslv1 = Sim.ts(DenseVector(50, 100), 0.0, 20.0, 0.05, Step.gillespie(mylv1))
Sim.plotTs(tslv1, "Gillespie simulation of LV1")

val mylv2 = lvparam(DenseVector(1.1, 0.01, 0.6))
val tslv2 = Sim.ts(DenseVector(50, 100), 0.0, 20.0, 0.05, Step.gillespie(mylv2))
Sim.plotTs(tslv2, "Gillespie simulation of LV2")

val mylv3: Spn[IntState] = lvparam()
val tslv3 = Sim.ts(DenseVector(50, 100), 0.0, 20.0, 0.05, Step.gillespie(mylv3))
Sim.plotTs(tslv3, "Gillespie simulation of LV3")
```
So, this is how we can define a SPN with a discrete state, intended for discrete stochastic simulation. By instead parameterising with a `DoubleState`, we can create models intended for continuous simulation, for example, using `Step.cle`. However, very often we want to use the same model for both discrete and continuous stochastic simulation. We can do that too, by making our creation function allow any state belonging to the `State` type class. Again, for the Lotka-Volterra model, the definition of the built-in model is:
```tut:silent
def lv[S: State](p: DenseVector[Double] = DenseVector(1.0, 0.005, 0.6)): Spn[S] =
  UnmarkedSpn[S](
    List("x", "y"),
    DenseMatrix((1, 0), (1, 1), (0, 1)),
    DenseMatrix((2, 0), (0, 2), (0, 0)),
    (x, t) => {
      val xd = x.toDvd
      DenseVector(
        xd(0) * p(0), xd(0) * xd(1) * p(1), xd(1) * p(2)
      )}
  )
```
Note the use of `.toDvd` to convert a state to a `DenseVector[Double]`, which is necessary since we do not require that all instances of the `State` type class are explicitly indexed. We can use this as we have already seen, by specifying the particular `State` to use for instantiation at call time.
```tut:silent
val lvDiscrete = lv[IntState]()
val tsDiscrete = Sim.ts(DenseVector(50, 100), 0.0, 20.0, 0.05, Step.gillespie(lvDiscrete))
Sim.plotTs(tsDiscrete, "Gillespie simulation of lvDiscrete")

val lvDiscrete2 = lv[IntState](DenseVector(1.1, 0.01, 0.6))
val tsDiscrete2 = Sim.ts(DenseVector(50, 100), 0.0, 20.0, 0.05, Step.gillespie(lvDiscrete2))
Sim.plotTs(tsDiscrete2, "Gillespie simulation of lvDiscrete2")

val lvCts = lv[DoubleState]()
val tsCts = Sim.ts(DenseVector(50.0, 100.0), 0.0, 20.0, 0.05, Step.cle(lvCts))
Sim.plotTs(tsCts, "Gillespie simulation of lvCts")
```
This approach allows us to define models that can be used for both discrete and continuous stochastic (and deterministic) simulations, and is therefore the recommended approach in cases where both discrete and continuous simulation makes sense.

## Next steps

Having worked through the tutorial, next it would make sense to first run and then study the code of the [examples](../examples/). In particular, the examples cover both spatial reaction-diffusion simulation and the problem of how to do parameter inference from data. After that, the [API documentation](https://darrenjw.github.io/scala-smfsb/api/smfsb/index.html) should make some sense. After that, studying the [source code](../src/main/scala/smfsb/) will be helpful. Looking at the [test code](../src/test/scala/) can also be useful. For more on Scala, and especially its use for scientific and statistical computing, take a look at my [Scala course](https://github.com/darrenjw/scala-course/blob/master/StartHere.md) and my [blog](https://darrenjw.wordpress.com/).


#### eof

