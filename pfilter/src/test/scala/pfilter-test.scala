/*
pfilter-test.scala

Test code for pfilter

 */

package pfilter

import org.scalatest._
import org.scalatest.junit._
import org.junit.runner.RunWith

import scala.language.higherKinds
import PFilter._

@RunWith(classOf[JUnitRunner])
class MyTestSuite extends FunSuite {

  test("1+2=3") {
    assert(1 + 2 === 3)
  }

  // test generic functions to check that the typeclass works as intended
  def doubleIt[C[_]: GenericColl](ca: C[Int]): C[Int] = ca map (_ * 2)
  def addThem[C[_]: GenericColl](ca: C[Int]): State = ca reduce (_ + _)
  def repeatThem[C[_]: GenericColl](ca: C[Int]): C[Int] = ca flatMap (x => List(x, x, x))
  def zipThem[C[_]: GenericColl](ci: C[Int], cd: C[Double]): C[(Int, Double)] = ci zip cd

  test("Vector in generic function including map") {
    val v = Vector(5, 10, 15, 20)
    val v2 = v map (_ * 2)
    val v3 = doubleIt(v)
    assert(v2 === v3)
  }

  test("Vector in generic function including flatMap") {
    val v = Vector(5, 10, 15)
    val v2 = v flatMap (x => Array(x, x, x))
    //println(v2)
    val v3 = repeatThem(v)
    assert(v2 === v3)
  }

  test("Vector in generic function including reduce") {
    val v = Vector(5, 10, 15)
    val s = addThem(v)
    assert(s === 30)
  }

  test("Vector in generic zipping function") {
    val v1 = Vector(1, 2, 3)
    val v2 = Vector(2.0, 4.0, 6.0)
    val v3 = v1 zip v2
    val v4 = zipThem(v1, v2)
    assert(v4 === v3)
  }

  test("ParVector in generic function including map") {
    val v = Vector(5, 10, 15, 30).par
    val v2 = v map (_ * 2)
    //println(v2)
    val v3 = doubleIt(v)
    assert(v2 === v3)
  }

  test("ParVector in generic function including flatMap") {
    val v = Vector(5, 10, 15, 10).par
    val v2 = v flatMap (x => Vector(x, x, x))
    //println(v2)
    val v3 = repeatThem(v)
    assert(v2 === v3)
  }

  test("ParVector in generic function including reduce") {
    val v = Vector(5, 10, 15).par
    val s = addThem(v)
    assert(s === 30)
  }

  test("ParVector in generic zipping function") {
    val v1 = Vector(1, 2, 3).par
    val v2 = Vector(2.0, 4.0, 6.0).par
    val v3 = v1 zip v2
    //println(v3)
    val v4 = zipThem(v1, v2)
    assert(v4 === v3)
  }



}

// eof
