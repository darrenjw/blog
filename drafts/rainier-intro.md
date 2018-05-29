# Monadic probabilistic programming in Scala with Rainier

```scala
set libraryDependencies += "com.stripe" %% "rainier-core" % "0.1.0"
set libraryDependencies += "com.cibo" %% "evilplot" % "0.2.0"
set resolvers += Resolver.bintrayRepo("cibotech", "public")
set scalaVersion := "2.12.4"
console
```

```scala
import com.stripe.rainier.compute._
import com.stripe.rainier.core._
import com.stripe.rainier.sampler._
import com.stripe.rainier.repl._
```

```scala
val r = new scala.util.Random(0)
val N = 1000
val beta0 = 0.1
val beta1 = 0.3
val x = (1 to N) map { i =>
  3.0 * r.nextGaussian
}
val theta = x map { xi =>
  beta0 + beta1 * xi
}
def expit(x: Double): Double = 1.0 / (1.0 + math.exp(-x))
val p = theta map expit
val y = p map (pi => if (r.nextDouble < pi) 1 else 0)
println(y.take(10))
println(x.take(10))
```

```scala
case class Bernoulli(p: Real) extends Distribution[Int] {

  def logDensity(b: Int): Real = {
    p.log * b + (Real.one - p).log * (1 - b)
  }

  val generator = Generator.from { (r, n) =>
    val pd = n.toDouble(p)
    val u = r.standardUniform
    if (u < pd) 1 else 0
  }

}
```

```scala
val model = for {
  beta0 <- Normal(0, 5).param
  beta1 <- Normal(0, 5).param
  _ <- Predictor.from{x: Double =>
      {
        val theta = beta0 + beta1 * x
        val p = Real(1.0) / (Real(1.0) + (Real(0.0) - theta).exp)
        //Binomial(p,1)
        Bernoulli(p)
      }
    }.fit(x zip y)
} yield (beta0, beta1)
```

```scala
implicit val rng = ScalaRNG(3)
val its = 10000
val thin = 5
val out = model.sample(HMC(5), 10000, its*thin, thin)
println(out.take(10))
```

```scala
import com.cibo.evilplot.plot._
import com.cibo.evilplot.colors._
import com.cibo.evilplot.plot.aesthetics.DefaultTheme._
import com.cibo.evilplot.numeric.Point

val data = out.map(_._1).zipWithIndex.map(p => Point(p._2,p._1))
val trace = LinePlot.series(data, "Line graph", HSL(210, 100, 56)).
  xAxis().yAxis().frame().hline(beta0).
  xLabel("Iteration").yLabel("b0").title("Trace plot")
val hist = Histogram(out.map(_._1),30).xAxis().yAxis().frame().
  xLabel("b0").yLabel("Frequency").vline(beta0)
val data1 = out.map(_._2).zipWithIndex.map(p => Point(p._2,p._1))
val trace1 = LinePlot.series(data1, "Line graph", HSL(210, 100, 56)).
  xAxis().yAxis().frame().hline(beta1).
  xLabel("Iteration").yLabel("b1").title("Trace plot")
val hist1 = Histogram(out.map(_._2),30).xAxis().yAxis().frame().
  xLabel("b1").yLabel("Frequency").vline(beta1)
val scatter = ScatterPlot(out.map(p => Point(p._1,p._2))).
  xAxis().yAxis().frame().vline(beta0).hline(beta1).
  xLabel("b0").yLabel("b1")
val contour = ContourPlot(out.map(p => Point(p._1,p._2))).
  xAxis().yAxis().frame().vline(beta0).hline(beta1).
  xLabel("b0").yLabel("b1")
val plot = Facets(Seq(Seq(trace,hist),Seq(trace1,hist1),Seq(scatter,contour)))
javax.imageio.ImageIO.write(plot.render().asBufferedImage, "png",
  new java.io.File("diagnostics.png"))
```



#### eof

