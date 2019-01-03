/*
Stub for scala-smfsb code
*/

object Stub {

  import smfsb._
  import breeze.linalg._
  import breeze.numerics._

  def main(args: Array[String]): Unit = {
    val model = SpnModels.lv[IntState]()
    val step = Step.gillespie(model)
    val ts = Sim.ts(DenseVector(50, 100), 0.0, 20.0, 0.05, step)
    Sim.plotTs(ts, "Gillespie simulation of LV model")
  }

}
