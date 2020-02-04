# Probabilistic programming with Rainier 0.3.0


```scala mdoc
import com.stripe.rainier.core._
import com.stripe.rainier.compute._

val a = Uniform(0,1).real
val b = a + 1

val c = Normal(b, a).real
Model.sample((a,c)).take(10)
```

```scala mdoc:image:scatter.png
import com.stripe.rainier.notebook._
val ac = Model.sample((a,c))
show("a", "c", scatter(ac))
```

```scala mdoc
val eggs = List[Long](45, 52, 45, 47, 41, 42, 44, 42, 46, 38, 36, 35, 41, 48, 42, 29, 45, 43, 45, 40, 42, 53, 31, 48, 40, 45, 39, 29, 45, 42)
val lambda = Gamma(0.5, 100).real
```

```scala mdoc:image:lambda.png
show("lambda", density(Model.sample(lambda)))
```

```scala mdoc
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
```

```scala mdoc:image:lambdap.png
show("lambda", density(posterior))
```



#### eof

