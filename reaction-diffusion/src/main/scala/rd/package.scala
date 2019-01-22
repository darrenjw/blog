/*
package.scala


 */

package object rd {

  import smfsb._
  import breeze.linalg.{Vector => BVec, _}
  import breeze.numerics._
  import scalafx.scene.image.WritableImage
  import scalafx.scene.paint._

  def toSfxI(im: PMatrix[DenseVector[Double]]): WritableImage = {
    val wi = new WritableImage(im.c, im.r)
    val pw = wi.pixelWriter
    val m = im.data.aggregate(0.0)((acc,v) => math.max(acc,max(v)), math.max(_,_))
    val rsi = im map (_ / m)
    (0 until im.c).par foreach (i =>
      (0 until im.r).par foreach (j =>
        pw.setColor(i, j, Color.rgb((rsi(i,j)(1)*255).toInt, 0, (rsi(i,j)(0)*255).toInt))
      ))
    wi
  }

  def toSfxIi(im: PMatrix[DenseVector[Int]]): WritableImage =
    toSfxI(im map (v => v map (_.toDouble)))

def sir[S: State](p: DenseVector[Double] = DenseVector(0.1, 0.5)): Spn[S] =
  UnmarkedSpn[S](
    List("S", "I", "R"),
    DenseMatrix((1, 1, 0), (0, 1, 0)),
    DenseMatrix((0, 2, 0), (0, 0, 1)),
    (x, t) => {
      val xd = x.toDvd
      DenseVector(
        xd(0) * xd(1) * p(0), xd(1) * p(1)
      )}
  )

  def toSfxI3(im: PMatrix[DenseVector[Double]]): WritableImage = {
    val wi = new WritableImage(im.c, im.r)
    val pw = wi.pixelWriter
    val m = im.data.aggregate(0.0)((acc,v) => math.max(acc,max(v)), math.max(_,_))
    val rsi = im map (_ / m)
    (0 until im.c).par foreach (i =>
      (0 until im.r).par foreach (j =>
        pw.setColor(i, j, Color.rgb((rsi(i,j)(1)*255).toInt, (rsi(i,j)(0)*255).toInt, (rsi(i,j)(2)*255).toInt))
      ))
    wi
  }



}

// eof

