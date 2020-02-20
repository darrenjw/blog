# Probabilistic programming with Rainier 0.3.0


```scala mdoc
import com.stripe.rainier.core._
import com.stripe.rainier.compute._

val a = Uniform(0,1).latent
val b = a + 1

val c = Normal(b, a).latent
Model.sample((a,c)).take(10)
```

```scala mdoc:image:scatter.png
import com.stripe.rainier.notebook._
val ac = Model.sample((a,c))
show("a", "c", scatter(ac))
```

```scala mdoc
val eggs = List[Long](45, 52, 45, 47, 41, 42, 44, 42, 46, 38, 36, 35, 41, 48, 42, 29, 45, 43, 45, 40, 42, 53, 31, 48, 40, 45, 39, 29, 45, 42)
val lambda = Gamma(0.5, 100).latent
```

```scala mdoc:image:lambda.png
show("lambda", density(Model.sample(lambda)))
```

```scala mdoc
val eggModel = Model.observe(eggs, Poisson(lambda))
eggModel.optimize(lambda)
val dozens = eggModel.optimize(lambda / 12)
import com.stripe.rainier.sampler._

val sampler = EHMC(warmupIterations = 5000, iterations = 5000)
val eggTrace = eggModel.sample(sampler)
eggTrace.diagnostics
val thinTrace = eggTrace.thin(2)
thinTrace.diagnostics
val posterior = eggTrace.predict(lambda)
```

```scala mdoc:image:lambdap.png
show("lambda", density(posterior))
```

# ANOVA model

```scala mdoc
    // simulate synthetic data
    implicit val rng = ScalaRNG(3)
    //val n = 50 // groups
    //val N = 150 // obs per group
    val n = 15 // groups
    val N = 50 // obs per group
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
    println("sampling...")
    val trace = model.sample(sampler)
    println("finished sampling.")
    val mt = trace.predict(m)
```

```scala mdoc:image:anova-mu.png
    show("mu", density(mt))
```

```scala mdoc:image:anova-sd.png
	val sDt = trace.predict(sD)
    show("sigD", density(sDt))
```

```scala mdoc:image:anova-se.png
	val sEt = trace.predict(sE)
    show("sigE", density(sEt))
```



#### eof

