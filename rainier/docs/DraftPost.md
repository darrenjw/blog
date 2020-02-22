# Probabilistic programming with Rainier 0.3.0


## Setup


**Start with setting up an SBT console from scratch? Bit messy due to non-standard resolvers...**


```scala mdoc
import com.stripe.rainier.core._
import com.stripe.rainier.compute._
import com.stripe.rainier.notebook._
import com.stripe.rainier.sampler._

implicit val rng = ScalaRNG(3)
val sampler = EHMC(warmupIterations = 5000, iterations = 5000)
```

# Normal random sample

Let's start by looking at inferring the mean and standard deviation of a normal random sample.


```scala mdoc
// first simulate some data
val n = 1000
val mu = 3.0
val sig = 5.0
val x = Vector.fill(n)(mu + sig*rng.standardNormal)
// now build Rainier model
val m = Normal(0,100).latent
val s = Gamma(1,10).latent
val nrs = Model.observe(x, Normal(m,s))
// now sample from the model
val out = nrs.sample(sampler)
```

```scala mdoc:image:nrs-mu.png
val mut = out.predict(m)
show("mu", density(mut))
```

```scala mdoc:image:nrs-sig.png
val sigt = out.predict(s)
show("sig", density(sigt))
```

# Logistic regression

Now let's fit a basic logistic regression model.

```scala mdoc:silent:reset
import com.stripe.rainier.core._
import com.stripe.rainier.compute._
import com.stripe.rainier.notebook._
import com.stripe.rainier.sampler._

implicit val rng = ScalaRNG(3)
val sampler = EHMC(warmupIterations = 5000, iterations = 5000)
```

```scala mdoc
val N = 1000
val beta0 = 0.1
val beta1 = 0.3
val x = (1 to N) map { _ => 2.0 * rng.standardNormal }
val theta = x map { xi => beta0 + beta1 * xi }
def expit(x: Double): Double = 1.0 / (1.0 + math.exp(-x))
val p = theta map expit
val yb = p map (pi => (rng.standardUniform < pi))
val y = yb map (b => if (b) 1L else 0L)
println("Proportion of successes: " + (y.filter(_ > 0L).length.toDouble/N))
// now build Rainier model
val b0 = Normal(0, 2).latent
val b1 = Normal(0, 2).latent
val model = Model.observe(y, Vec.from(x).map(xi => {
    val theta = b0 + b1*xi
    val p  = 1.0 / (1.0 + (-theta).exp)
    Bernoulli(p)
}))
// now sample from the model
val bt = model.sample(sampler)
```

```scala mdoc:image:lr-b0.png
val b0t = bt.predict(b0)
show("b0", density(b0t))
```

```scala mdoc:image:lr-b1.png
val b1t = bt.predict(b1)
show("b1", density(b1t))
```


# ANOVA model

Let's now turn attention to a very basic normal random effects model.

```scala mdoc:silent:reset
import com.stripe.rainier.core._
import com.stripe.rainier.compute._
import com.stripe.rainier.notebook._
import com.stripe.rainier.sampler._

implicit val rng = ScalaRNG(3)
val sampler = EHMC(warmupIterations = 5000, iterations = 5000)
```

```scala mdoc
// simulate synthetic data
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
val anova = models.reduce{(m1, m2) => m1.merge(m2)}
// now sample the model
val trace = anova.sample(sampler)
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

