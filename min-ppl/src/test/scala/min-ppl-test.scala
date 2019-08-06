/*
min-ppl-test.scala

Some basic sanity checks on the language

 */

import org.scalatest.flatspec.AnyFlatSpec
import org.scalactic._
import MinPpl._
import breeze.stats.{meanAndVariance => meanVar}

class PplSpec extends AnyFlatSpec with Tolerance {

  "A linear Gaussian" should "flatMap correctly" in {
    val xy = for {
      x <- Normal(5,4)
      y <- Normal(x,1)
    } yield (x,y)
    val y = xy.map(_._2).empirical
    val mv = meanVar(y)
    assert(y.length === 1000)
    assert(mv.mean === 5.0 +- 0.3)
    assert(mv.variance === 5.0 +- 0.8)
  }

  it should "cond correctly" in {
    val xy = for {
      x <- Normal(5,4)
      y <- Normal(x,1)
    } yield (x,y)
    val y = xy.map(_._2)
    val yGz = y.cond(yi => Normal(yi, 9).ll(8.0)).empirical
    val mv = meanVar(yGz)
    assert(mv.mean === 5.857 +- 0.3)
    assert(mv.variance === 2.867 +- 0.8)
    val xyGz = xy.cond{case (x,y) => Normal(y,9).ll(8.0)}.empirical
    val mvx = meanVar(xyGz.map(_._1))
    assert(mvx.mean === 5.857 +- 0.3)
    assert(mvx.variance === 2.867 +- 0.8)
    val mvy = meanVar(xyGz.map(_._2))
    assert(mvy.mean === 6.071 +- 0.3)
    assert(mvy.variance === 3.214 +- 0.8)
  }

  it should "cond correctly in a for" in {
    val wxyz = for {
      w <- Normal(5,2)
      x <- Normal(w,2)
      y <- Normal(x,1).cond(y => Normal(y,9).ll(8.0))
    } yield (w,x,y)
    val wxyze = wxyz.empirical
    val mvw = meanVar(wxyze.map(_._1))
    assert(mvw.mean === 5.429 +- 0.3)
    assert(mvw.variance === 1.714 +- 0.8)
    val mvx = meanVar(wxyze.map(_._2))
    assert(mvx.mean === 5.857 +- 0.3)
    assert(mvx.variance === 2.867 +- 0.8)
    val mvy = meanVar(wxyze.map(_._3))
    assert(mvy.mean === 6.071 +- 0.3)
    assert(mvy.variance === 3.214 +- 0.8)
  }

  it should "fit correctly" in {
    val xyzf = for {
      x <- Normal(5,4)
      y <- Normal(x,1)
      z <- Normal(y,9).fit(8.0)
    } yield (x,y,z)
    val xyzfe = xyzf.empirical
    val mvx = meanVar(xyzfe.map(_._1))
    assert(mvx.mean === 5.857 +- 0.3)
    print("x: 5.857, 2.867 : ")
    println(meanVar(xyzfe.map(_._1))) // x
    print("y: 6.071, 3.214 : ")
    println(meanVar(xyzfe.map(_._2))) // y
    print("z: 6.071,12.214 : ")
    println(meanVar(xyzfe.map(_._3))) // z
  }


}

