/*
reaction-diffusion.scala

*/

object LvCle {

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
    (0 until im.c) foreach (i =>
      (0 until im.r) foreach (j =>
        pw.setColor(i, j, Color.rgb((rsi(i,j)(1)*255).toInt, 0, (rsi(i,j)(0)*255).toInt))
      ))
    wi
  }


  def main(args: Array[String]): Unit = {
    val r = 300; val c = 400
    val model = SpnModels.lv[DoubleState]()
    val step = Spatial.cle2d(model, DenseVector(0.6, 0.6), 0.05)
    val x00 = DenseVector(0.0, 0.0)
    val x0 = DenseVector(50.0, 100.0)
    val xx00 = PMatrix(r, c, Vector.fill(r*c)(x00))
    val xx0 = xx00.updated(c/2, r/2, x0)
    val s = Stream.iterate(xx0)(step(_,0.0,0.1))
    val si = s map (toSfxI(_))
    scalaview.SfxImageViewer(si, 1, autoStart=true)
  }

}

// eof
