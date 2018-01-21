/*
comonads.scala
Examples for comonads blog post

*/

object ComonadExamples {

  import cats._
  import cats.implicits._

  // linear filter for Stream
  def linearFilterS(weights: Stream[Double])(s: Stream[Double]): Double =
    (weights, s).parMapN(_*_).sum

  // a generic linear filter
  // (with help from Fabio Labella @SystemFw on Cats gitter channel)
  def linearFilter[F[_]: Foldable, G[_]](
    weights: F[Double], s: F[Double]
  )(implicit ev: NonEmptyParallel[F, G]): Double =
    (weights, s).parMapN(_*_).fold

  def filterExamples: Unit = {
    // define a steam (logistic map)
    val lam = 3.7
    def s = Stream.iterate(0.5)(x => lam*x*(1-x))
    println(s.take(10).toList)
    println(s.coflatten.take(3).map(_.take(4).toList).toList)
    // apply a filter
    println(s.coflatMap(linearFilterS(Stream(0.25,0.5,0.25))).take(5).toList)
    // now use generic filter
    def myFilter(s: Stream[Double]): Double =
      linearFilter(Stream(0.25, 0.5, 0.25),s)
    println(s.coflatMap(myFilter).take(5).toList)
    // using a lambda
    println(s.coflatMap(s => linearFilter(Stream(0.25,0.5,0.25),s)).
      take(5).toList)
    // check stack safety
    println(s.coflatMap(s => linearFilter(Stream(0.25,0.5,0.25),s)).
      take(1000000).drop(999995).toList)
    // use a List instead of a Stream
    val sl = s.take(10).toList
    println(sl)
    println(sl.coflatMap(sl => linearFilter(List(0.25,0.5,0.25),sl)) )
    // some plots in breeze...
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
  }




  // Numerical solution of the 2d heat equation PDE
  // Or image processing, depending on your point of view

  // https://jaspervdj.be/posts/2014-11-27-comonads-image-processing.html
  // I'm going to go for column-major rather than row-major

  // Basic image class
  case class Image[T](w: Int, h: Int, data: Vector[T]) {
    def apply(x: Int, y: Int): T = data(x*h+y)
    def map[S](f: T => S): Image[S] = Image(w, h, data map f)
    def updated(x: Int, y: Int, value: T): Image[T] =
      Image(w,h,data.updated(x*h+y,value))
  }

  // Pointed image (with a focus/cursor)
  case class PImage[T](x: Int, y: Int, image: Image[T]) {
    def extract: T = image(x, y)
    def map[S](f: T => S): PImage[S] = PImage(x, y, image map f)
    def coflatMap[S](f: PImage[T] => S): PImage[S] = PImage(
      x, y, Image(image.w, image.h,
      (0 until (image.w * image.h)).toVector.map(i => {
        val xx = i / image.h
        val yy = i % image.h
        f(PImage(xx, yy, image))
      })))
    // now a few methods for navigation - not part of the comonad interface
    // using periodic boundary conditions
    def up: PImage[T] = {
      val py = y-1
      val ny = if (py >= 0) py else (py + image.h)
      PImage(x,ny,image)
    }
    def down: PImage[T] = {
      val py = y+1
      val ny = if (py < image.h) py else (py - image.h)
      PImage(x,ny,image)
    }
    def left: PImage[T] = {
      val px = x-1
      val nx = if (px >= 0) px else (px + image.w)
      PImage(nx,y,image)
    }
    def right: PImage[T] = {
      val px = x+1
      val nx = if (px < image.w) px else (px - image.w)
      PImage(nx,y,image)
    }
  }

  // Provide evidence that PImage is a Cats Comonad
  implicit val pimageComonad = new Comonad[PImage] {
    def extract[A](wa: PImage[A]) = wa.extract
    def coflatMap[A,B](wa: PImage[A])(f: PImage[A] => B): PImage[B] =
      wa.coflatMap(f)
    def map[A,B](wa: PImage[A])(f: A => B): PImage[B] = wa.map(f)
  }

  // convert to and from Breeze matrices
  import breeze.linalg.{Vector => BDV, _}
  def BDM2I[T](m: DenseMatrix[T]): Image[T] =
    Image(m.cols, m.rows, m.data.toVector)
  def I2BDM(im: Image[Double]): DenseMatrix[Double] = {
    new DenseMatrix(im.h,im.w,im.data.toArray)
  }

  // examples
  def imageExamples: Unit = {
    // a filter correspoding to one step of integration of the heat equation
    def fil(pi: PImage[Double]): Double = (2*pi.extract+pi.up.extract+pi.down.extract+pi.left.extract+pi.right.extract)/6.0
    // simulate a noisy image
    import breeze.stats.distributions.Gaussian
    val bdm = DenseMatrix.tabulate(200,250){case (i,j) => math.cos(0.1*math.sqrt((i*i+j*j))) + Gaussian(0.0,1.0).draw}
    val pim0 = PImage(0,0,BDM2I(bdm))
    // just checking that Cats Comonad syntax is picked up - not required
    pim0.coflatten 
    // use filter to generate an infinite stream of pointed images
    def pims = Stream.iterate(pim0)(_.coflatMap(fil))
    // render the first few images from the stream
    import breeze.plot._
    val fig = Figure("Diffusing a noisy image")
    pims.take(25).zipWithIndex.foreach{case (pim,i) => {
      val p = fig.subplot(5,5,i)
      p += image(I2BDM(pim.image))
    }}


  }


  def main(args: Array[String]): Unit = {
    filterExamples
    imageExamples
  }


}



// eof
