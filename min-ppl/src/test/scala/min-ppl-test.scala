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
    System.err.println("**These tests take a LONG time, and it is normal for a couple to fail**")
    val xy = for {
      x <- Normal(5,4)
      y <- Normal(x,1)
    } yield (x,y)
    val y = xy.map(_._2).empirical
    val mv = meanVar(y)
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
    assert(mvx.variance === 2.867 +- 0.8)
    val mvy = meanVar(xyzfe.map(_._2))
    assert(mvy.mean === 6.071 +- 0.3)
    assert(mvy.variance === 3.214 +- 0.8)
    val mvz = meanVar(xyzfe.map(_._3))
    assert(mvz.mean === 6.071 +- 0.3)
    assert(mvz.variance === 12.214 +- 1.8)
  }

  it should "fitQ correctly" in {
    val xyzfq = for {
      x <- Normal(5,4)
      y <- Normal(x,1)
      z <- Normal(y,9).fitQ(8.0)
    } yield (x,y,z)
    val xyzfqe = xyzfq.empirical
    val mvx = meanVar(xyzfqe.map(_._1))
    assert(mvx.mean === 5.857 +- 0.3)
    assert(mvx.variance === 2.867 +- 0.8)
    val mvy = meanVar(xyzfqe.map(_._2))
    assert(mvy.mean === 6.071 +- 0.3)
    assert(mvy.variance === 3.214 +- 0.8)
    val mvz = meanVar(xyzfqe.map(_._3))
    assert(mvz.mean === 8.000 +- 0.001)
    assert(mvz.variance === 0.000 +- 0.001)
  }

  it should "fit marginalised correctly" in {
    val yzf = for {
      y <- Normal(5,5)
      z <- Normal(y,9).fit(8.0)
    } yield (y,z)
    val yzfe = yzf.empirical
    val mvy = meanVar(yzfe.map(_._1))
    assert(mvy.mean === 6.071 +- 0.3)
    assert(mvy.variance === 3.213 +- 0.8)
    val mvz = meanVar(yzfe.map(_._2))
    assert(mvz.mean === 6.071)
    assert(mvz.variance === 12.214)
  }

  it should "fit multiple iid observations correctly" in {
    val yzf2 = for {
      y <- Normal(5,5)
      z <- Normal(y,18).fit(List(6.0,10.0))
    } yield (y,z)
    val yzfe2 = yzf2.empirical
    val mvy = meanVar(yzfe2.map(_._1))
    assert(mvy.mean === 6.071 +- 0.3)
    assert(mvy.variance === 3.214 +- 0.8)
    val mvz = meanVar(yzfe2.map(_._2))
    assert(mvz.mean === 6.071 +- 0.3)
    assert(mvz.variance === 21.214 +- 2.5)
  }

  it should "deep chain correctly" in {
    val deep = for {
      w <- Normal(2.0,1.0)
      x <- Normal(w,1)
      y <- Normal(x,2)
      z <- Normal(y,1)
    } yield z
    val mvz = meanVar(deep.empirical)
    assert(mvz.mean === 2.0 +- 0.3)
    assert(mvz.variance === 5.0 +- 0.8)
  }

}

