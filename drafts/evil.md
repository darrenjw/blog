# Using EvilPlot with scala-view

## EvilPlot

[EvilPlot](https://cibotech.github.io/evilplot/) is a new functional data visualisation library for Scala. Although there are several data viz options for Scala, this new library has a nice functional API for producing attractive, flexible, compositional plots which can be rendered in JVM applications and in web applications (via Scala.js). For a quick introduction, see this [blog post](https://medium.com/@CiboEng/evilplot-a-combinator-based-plotting-library-for-scala-4531f00208) from one of the library's creators. For further information, see the [official documentation](https://cibotech.github.io/evilplot/) and the [github repo](https://github.com/cibotech/evilplot). For a quick overview of the kinds of plots that the library is capable of generating, see the [plot catalog](https://cibotech.github.io/evilplot/plot-catalog.html).

The library is designed to produce plots which can be rendered into applications. However, when doing data analysis in the REPL on the JVM, it is often convenient to be able to just pop up a plot in a window on the desktop. EvilPlot doesn't seem to contain code for on-screen rendering, but the plots can be rendered to a bitmap image. In the [previous post](https://darrenjw.wordpress.com/2018/03/01/scala-view-animate-streams-of-images/) I described a small library, [scala-view](https://github.com/darrenjw/scala-view/), which renders such images, and image sequences on the desktop. In this post I'll walk through using scala-view to render EvilPlot plots on-screen.

## An interactive session

To follow this session, you just need to run [SBT](https://www.scala-sbt.org/) from an empty directory. Just run `sbt` and paste the following at the SBT prompt:
```scala
set libraryDependencies += "com.cibo" %% "evilplot" % "0.2.0"
set libraryDependencies += "com.github.darrenjw" %% "scala-view" % "0.6-SNAPSHOT"
set resolvers += Resolver.bintrayRepo("cibotech", "public")
set resolvers += "Sonatype Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots/"
set scalaVersion := "2.12.4"
set fork := true
console
```

### Displaying a single plot

This will give a Scala REPL prompt. First we need some imports:
```scala
import com.cibo.evilplot.plot._
import com.cibo.evilplot.colors._
import com.cibo.evilplot.plot.aesthetics.DefaultTheme._
import com.cibo.evilplot.numeric.Point
import java.awt.Image.SCALE_SMOOTH
import scalaview.Utils._
```

We can simulate some data an produce a simple line chart:
```scala
val data = Seq.tabulate(100) { i =>
  Point(i.toDouble, scala.util.Random.nextDouble())
}
val plot = LinePlot.series(data, "Line graph", HSL(210, 100, 56)).
  xAxis().yAxis().frame().
  xLabel("x").yLabel("y").render()
```

This `plot` object contains the rendering instructions, but doesn't actually produce a plot. We can use scala-view to display it as follows:
```scala
scalaview.SfxImageViewer(biResize(plot.asBufferedImage,1000,800,SCALE_SMOOTH))
```
This will produce a window on screen something like the following:

PLOT HERE

Don't close this plot yet, as this will confuse the REPL. Just switch back to the REPL and continue.

### Displaying a sequence of plots

Sometimes we want to produce a sequence of plots. Let's now suppose that the data above arises sequentially as a stream, and that we want to produce a sequence of plots with each observation as it arrives. First create a stream of partial datasets and map a function which turns a dataset into a plot to get a stream of images representing the plots. Then pass the stream of images into the viewer to get an animated sequence of plots on-screen

```scala
val dataStream = data.toStream
val cumulStream = dataStream.scanLeft(Nil: List[Point])((l,p) => p :: l).drop(1)
def dataToImage(data: List[Point]) = LinePlot.
  series(data, "Line graph", HSL(210, 100, 56)).
    xAxis().yAxis().frame().
    xLabel("x").yLabel("y").render().asBufferedImage
val plotStream = cumulStream map (d => biResize(dataToImage(d),1000,800,SCALE_SMOOTH))
scalaview.SfxImageViewer.bi(plotStream, 100000, autoStart=true)
```



#### eof
