/*
comonads.scala
Examples for comonads blog post

*/

object ComonadExamples {

  import cats._
  import cats.implicits._

  def linearFilterS(weights: Stream[Double])(s: Stream[Double]): Double =
    (weights, s).parMapN(_*_).sum

  def linearFilter[F[_]: Foldable, G[_]](
    weights: F[Double], s: F[Double]
  )(implicit ev: NonEmptyParallel[F, G]): Double =
    (weights, s).parMapN(_*_).fold

  def filterExamples: Unit = {
    // def s = Stream.iterate(0.0)(_+1.0)
    def s = Stream.iterate(0.5)(x => 3.0*x*(1-x))
    println(s.take(10).toList)
    println(s.coflatten.take(3).map(_.take(4).toList).toList)

    println(s.coflatMap(linearFilterS(Stream(0.25,0.5,0.25))).take(5).toList)

    def myFilter(s: Stream[Double]): Double = linearFilter(Stream(0.25, 0.5, 0.25),s)
    println(s.coflatMap(myFilter).take(5).toList)

    println(s.coflatMap(s => linearFilter(Stream(0.25,0.5,0.25),s)).take(5).toList)
    println(s.coflatMap(s => linearFilter(Stream(0.25,0.5,0.25),s)).take(1000000).drop(999995).toList)


    val sl = s.take(10).toList
    println(sl)
    println( sl.coflatMap(sl => linearFilter(List(0.25,0.5,0.25),sl)) )
  }


  /*
  // Stream Comonad instance - need to define "extract" and "coflatMap"...
  // ASSUMING that the Stream is INFINTE
  implicit val streamComonad = new Comonad[Stream] {
    def extract[A](wa: Stream[A]) = wa.head
    def coflatMap[A,B](wa: Stream[A])(f: Stream[A] => B): Stream[B] =
      f(wa) #:: coflatMap(wa.tail)(f)
    def map[A,B](wa: Stream[A])(f: A => B): Stream[B] = wa map f
  }
   */


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

  import breeze.linalg.{Vector => BDV, _}
  def BDM2I[T](m: DenseMatrix[T]): Image[T] =
    Image(m.cols, m.rows, m.data.toVector)

  def I2BDM(im: Image[Double]): DenseMatrix[Double] = {
    new DenseMatrix(im.h,im.w,im.data.toArray)
  }

  def imageExamples: Unit = {
    val pi = PImage(0,0,Image(3,3,(1 to 9).map(_.toDouble).toVector))
    println(pi)
    def fil(pi: PImage[Double]): Double = 0.2*(pi.extract+pi.up.extract+pi.down.extract+pi.left.extract+pi.right.extract)
    val pif = pi.coflatMap(fil)
    println(pif)

    import breeze.stats.distributions.Gaussian
    val bdm = DenseMatrix.tabulate(500,800){case (i,j) => math.cos(0.02*math.sqrt((i*i+j*j)))} + DenseMatrix.rand(500,800)
    import breeze.plot._
    val fig = Figure("Random matrix")
    val p1 = fig.subplot(2,2,0)
    p1 += image(bdm)
    val p2 = fig.subplot(2,2,1)
    p2 += image(I2BDM(PImage(0,0,BDM2I(bdm)).coflatMap(fil).image))
    val p3 = fig.subplot(2,2,2)
    p3 += image(I2BDM(PImage(0,0,BDM2I(bdm)).coflatMap(fil).coflatMap(fil).image))
    val p4 = fig.subplot(2,2,3)
    p4 += image(I2BDM(PImage(0,0,BDM2I(bdm)).coflatMap(fil).coflatMap(fil).coflatMap(fil).image))


  }


  def main(args: Array[String]): Unit = {
    filterExamples
    imageExamples
  }


}



// eof
