/*
rainier.scala
Simple example rainier app
*/

object RainierApp {

  import cats._
  import cats.implicits._

  def main(args: Array[String]): Unit = {

    import com.stripe.rainier.core._
    import com.stripe.rainier.compute._

    val a = Uniform(0,1).real
    val b = a + 1

    val c = Normal(b, a).real
    Model.sample((a,c)).take(10)

    import com.stripe.rainier.notebook._
    val ac = Model.sample((a,c))
    show("a", "c", scatter(ac)) // produces an almond Image, but then what?

    import com.cibo.evilplot._
    import com.cibo.evilplot.plot._
    displayPlot(scatter(ac).render())

  }

}
