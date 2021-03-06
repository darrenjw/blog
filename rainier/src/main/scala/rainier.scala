/*
rainier.scala
Simple example rainier app
*/

object RainierApp {

  import cats._
  import cats.implicits._
  import com.stripe.rainier.core._
  import com.stripe.rainier.compute._
  import com.stripe.rainier.sampler._
  import com.stripe.rainier.notebook._
  import com.cibo.evilplot._
  import com.cibo.evilplot.plot._

  // rainier tutorial
  def tutorial: Unit = {
    println("Tutorial")
    val a = Uniform(0,1).latent
    val b = a + 1

    val c = Normal(b, a).latent
    Model.sample((a,c)).take(10)

    val ac = Model.sample((a,c))
    show("a", "c", scatter(ac)) // produces an almond Image, but then what?
    displayPlot(scatter(ac).render()) // use Evilplot to display on console

    val eggs = List[Long](45, 52, 45, 47, 41, 42, 44, 42, 46, 38, 36, 35, 41, 48, 42, 29, 45, 43,
      45, 40, 42, 53, 31, 48, 40, 45, 39, 29, 45, 42)
    val lambda = Gamma(0.5, 100).latent

    show("lambda", density(Model.sample(lambda)))  // show
    mcmcSummary("lambda", Model.sample(lambda))

    val eggModel = Model.observe(eggs, Poisson(lambda))
    eggModel.optimize(lambda)
    val dozens = eggModel.optimize(lambda / 12)

    val sampler = EHMC(5000, 500)
    val eggTrace = eggModel.sample(sampler)
    eggTrace.diagnostics
    val thinTrace = eggTrace.thin(2)
    thinTrace.diagnostics
    val posterior = eggTrace.predict(lambda)

    show("lambda", density(posterior))  // show
    mcmcSummary("lambda", posterior)
  }

  def mcmcSummary(name: String, chain: Seq[Double]): Unit = {
    println(name)
    val dens = density(chain).
      standard().
      xLabel(name).
      yLabel("Density")
    val trace = line(chain.zipWithIndex map (_.swap)).
      standard().
      xLabel("Iteration").
      yLabel(name)
    //displayPlot(dens.render())
    //displayPlot(trace.render())
    displayPlot(Facets(Vector(Vector(trace, dens))).render())
  }

  def mcmcSummary(chain: Seq[Double]): Unit = mcmcSummary("Variable", chain)


  // normal random sample
  def nrs: Unit = {
    // first simulate some synthetic data
    val n = 1000
    val mu = 3.0
    val sig = 5.0
    implicit val rng = ScalaRNG(3)
    val x = Vector.fill(n)(mu + sig*rng.standardNormal)
    // now build Rainier model
    val m = Normal(0,100).latent
    val s = Gamma(1,10).latent
    val model = Model.observe(x, Normal(m,s))
    // now sample from the model
    val sampler = EHMC(5000, 5000)
    println("sampling...")
    val out = model.sample(sampler)
    println("finished sampling.")
    println(out.diagnostics)
    val mut = out.predict(m)
    show("mu", density(mut))
    val sigt = out.predict(s)
    show("sig", density(sigt))
    // try some diagnostic plots
    mcmcSummary("mu", mut)
    mcmcSummary("sig", sigt)
  }

  // logistic regression
  def logReg: Unit = {
    println("logReg")
    // first simulate some data from a logistic regression model
    implicit val rng = ScalaRNG(3)
    val N = 1000
    val beta0 = 0.1
    val beta1 = 0.3
    val x = (1 to N) map { _ =>
      3.0 * rng.standardNormal
    }
    val theta = x map { xi =>
      beta0 + beta1 * xi
    }
    def expit(x: Double): Double = 1.0 / (1.0 + math.exp(-x))
    val p = theta map expit
    val yb = p map (pi => (rng.standardUniform < pi))
    val y = yb map (b => if (b) 1L else 0L)
    println(y.take(10))
    println(x.take(10))
    // now build Rainier model
    val b0 = Normal(0, 5).latent
    val b1 = Normal(0, 5).latent
    val model = Model.observe(y, Vec.from(x).map{xi => 
      val theta = b0 + b1*xi
      val p  = 1.0 / (1.0 + (-theta).exp)
      Bernoulli(p)
    })
    // now sample from the model
    //val sampler = EHMC(10000, 1000)
    val sampler = HMC(5000, 1000, 50)
    println("sampling...")
    val bt = model.sample(sampler)
    println("finished sampling.")
    println(bt.diagnostics)
    val b0t = bt.predict(b0)
    show("b0", density(b0t))
    val b1t = bt.predict(b1)
    show("b1", density(b1t))
    mcmcSummary("b0", b0t)
    mcmcSummary("b1", b1t)
  }

  // one-way anova model
  def anova: Unit = {
    println("anova")
    // simulate synthetic data
    implicit val rng = ScalaRNG(3)
    //val n = 50 // groups
    //val N = 150 // obs per group
    val n = 10 // groups
    val N = 20 // obs per group
    val mu = 5.0 // overall mean
    val sigE = 2.0 // random effect SD
    val sigD = 3.0 // obs SD
    val effects = Vector.fill(n)(sigE * rng.standardNormal)
    val data = effects map (e =>
      Vector.fill(N)(mu + e + sigD * rng.standardNormal))
    // build model
    val m = Normal(0, 100).latent
    val sD = LogNormal(0, 10).latent
    val sE = LogNormal(1, 5).latent
    val eff = Vector.fill(n)(Normal(m, sE).latent)
    val models = (0 until n).map(i =>
      Model.observe(data(i), Normal(eff(i), sD)))
    val model = models.reduce{(m1, m2) => m1.merge(m2)}
    // now sample the model
    val sampler = EHMC(5000, 5000)
    println("sampling...")
    val trace = model.sample(sampler)
    println("finished sampling.")
    println(trace.diagnostics)
    val mt = trace.predict(m)
    show("mu", density(mt))
    mcmcSummary("mu", mt)
    mcmcSummary("sigE", trace.predict(sE))
    mcmcSummary("sigD", trace.predict(sD))
  }

  // repeat one-way anova, but without merging
  def anova2: Unit = {

  }


  def main(args: Array[String]): Unit = {
    println("main starting")

    //tutorial
    //nrs
    logReg
    //anova


    println("main finishing")
  }

}
