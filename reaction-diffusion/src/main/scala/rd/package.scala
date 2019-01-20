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

}

// eof

