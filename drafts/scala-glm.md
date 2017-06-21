# scala-glm: Regression modelling in Scala

## Introduction

As discussed in the [previous post](https://darrenjw.wordpress.com/2017/05/31/statistical-computing-with-scala-free-on-line-course/), I've recently constructed and delivered a short course on statistical computing with Scala. Much of the course is concerned with writing statistical algorithms in Scala, typically making use of the scientific and numerical computing library, [Breeze](https://github.com/scalanlp/breeze). Breeze has all of the essential tools necessary for building statistical algorithms, but doesn't contain any higher level modelling functionality. As part of the course, I walked through how to build a small library for regression modelling on top of Breeze, including all of the usual regression diagnostics (such as standard errors, t-statistics, p-values, F-statistics, etc.). While preparing the course materials it occurred to me that it would be useful to package and document this code properly for general use. In advance of the course I packaged the code up into a bare-bones library, but since then I've fleshed it out, tidied it up and documented it properly, so it's now ready for people to use.

The library covers PCA, linear regression modelling and simple one-parameter GLMs (including logistic and Poisson regression). The underlying algorithms are fairly efficient and numerically stable (eg. linear regression uses the [QR decomposition](https://en.wikipedia.org/wiki/QR_decomposition) of the model matrix, and the GLM fitting uses QR within each [IRLS](https://en.wikipedia.org/wiki/Iteratively_reweighted_least_squares) step), though they are optimised more for clarity than speed. The library also includes a few utility functions and procedures, including a pairs plot (scatter-plot matrix).

## A linear regression example

Plenty of [documentation](https://github.com/darrenjw/scala-glm/blob/master/README.md) is available from the [scala-glm github repo](https://github.com/darrenjw/scala-glm) which I won't repeat here. But to give a rough idea of how things work, I'll run through an interactive session for the linear regression example.

First, download a [dataset](https://archive.ics.uci.edu/ml/datasets/airfoil+self-noise) from the [UCI ML Repository](http://archive.ics.uci.edu/ml/) to disk for subsequent analysis (caching the file on disk is good practice, as it avoids unnecessary load on the UCI server, and allows running the code off-line).

```scala
import scalaglm._
import breeze.linalg._

val url = "http://archive.ics.uci.edu/ml/machine-learning-databases/00291/airfoil_self_noise.dat"
val fileName = "self-noise.csv"

// download the file to disk if it hasn't been already
val file = new java.io.File(fileName)
if (!file.exists) {
  val s = new java.io.PrintWriter(file)
  val data = scala.io.Source.fromURL(url).getLines
  data.foreach(l => s.write(l.trim.
    split('\t').filter(_ != "").
    mkString("", ",", "\n")))
  s.close
}
```

Once we have a CSV file on disk, we can load it up and look at it.
```scala
val mat = csvread(new java.io.File(fileName))
// mat: breeze.linalg.DenseMatrix[Double] =
// 800.0    0.0  0.3048  71.3  0.00266337  126.201
// 1000.0   0.0  0.3048  71.3  0.00266337  125.201
// 1250.0   0.0  0.3048  71.3  0.00266337  125.951
// ...
println("Dim: " + mat.rows + " " + mat.cols)
// Dim: 1503 6
val figp = Utils.pairs(mat, List("Freq", "Angle", "Chord", "Velo", "Thick", "Sound"))
// figp: breeze.plot.Figure = breeze.plot.Figure@37718125
```

We can then regress the response in the final column on the other variables.

```scala
val y = mat(::, 5) // response is the final column
// y: DenseVector[Double] = DenseVector(126.201, 125.201, ...
val X = mat(::, 0 to 4)
// X: breeze.linalg.DenseMatrix[Double] =
// 800.0    0.0  0.3048  71.3  0.00266337
// 1000.0   0.0  0.3048  71.3  0.00266337
// 1250.0   0.0  0.3048  71.3  0.00266337
// ...
val mod = Lm(y, X, List("Freq", "Angle", "Chord", "Velo", "Thick"))
// mod: scalaglm.Lm =
// Lm(DenseVector(126.201, 125.201, ...
mod.summary
// Estimate	 S.E.	 t-stat	p-value		Variable
// ---------------------------------------------------------
// 132.8338	 0.545	243.866	0.0000 *	(Intercept)
//  -0.0013	 0.000	-30.452	0.0000 *	Freq
//  -0.4219	 0.039	-10.847	0.0000 *	Angle
// -35.6880	 1.630	-21.889	0.0000 *	Chord
//   0.0999	 0.008	12.279	0.0000 *	Velo
// -147.3005	15.015	-9.810	0.0000 *	Thick
// Residual standard error:   4.8089 on 1497 degrees of freedom
// Multiple R-squared: 0.5157, Adjusted R-squared: 0.5141
// F-statistic: 318.8243 on 5 and 1497 DF, p-value: 0.00000
val fig = mod.plots
// fig: breeze.plot.Figure = breeze.plot.Figure@60d7ebb0
```

There is a `.predict` method for generating point predictions (and standard errors) given a new model matrix, and fitting GLMs is very similar - these things are covered in the [quickstart guide](https://darrenjw.github.io/scala-glm/QuickStart.html) for the library.

## Summary

[scala-glm](https://github.com/darrenjw/scala-glm/) is a small Scala library built on top of the [Breeze](https://github.com/scalanlp/breeze) numerical library which enables simple and convenient regression modelling in Scala. It is reasonably well documented and usable in its current form, but I intend to gradually add additional features according to demand as time permits.

#### eof

