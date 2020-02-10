/*
rainier.scala
Simple example rainier app
*/

object RainierApp {

  import cats._
  import cats.implicits._

  def main(args: Array[String]): Unit = {

    println("hi")

    import com.stripe.rainier.core._
    import com.stripe.rainier.compute._

    val a = Uniform(0,1).latent
    val b = a + 1

    val c = Normal(b, a).latent
    Model.sample((a,c)).take(10)

    import com.stripe.rainier.notebook._
    val ac = Model.sample((a,c))
    show("a", "c", scatter(ac)) // produces an almond Image, but then what?

    import com.cibo.evilplot._
    import com.cibo.evilplot.plot._
    displayPlot(scatter(ac).render())

    val eggs = List[Long](45, 52, 45, 47, 41, 42, 44, 42, 46, 38, 36, 35, 41, 48, 42, 29, 45, 43,
      45, 40, 42, 53, 31, 48, 40, 45, 39, 29, 45, 42)
    val lambda = Gamma(0.5, 100).latent

    show("lambda", density(Model.sample(lambda)))  // show
    displayPlot(density(Model.sample(lambda)).render())

    val eggModel = Model.observe(eggs, Poisson(lambda))
    eggModel.optimize(lambda)
    val dozens = eggModel.optimize(lambda / 12)
    import com.stripe.rainier.sampler._

    val sampler = EHMC(warmupIterations = 5000, iterations = 500)
    val eggTrace = eggModel.sample(sampler)
    eggTrace.diagnostics
    val thinTrace = eggTrace.thin(2)
    thinTrace.diagnostics
    val posterior = eggTrace.predict(lambda)

    show("lambda", density(posterior))  // show
    displayPlot(density(posterior).render())


    println("bye")

  }

}
