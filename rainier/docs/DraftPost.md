# Probabilistic programming with Rainier 0.3


```scala mdoc
import com.stripe.rainier.core._
import com.stripe.rainier.compute._

val a = Uniform(0,1).real
val b = a + 1

val c = Normal(b, a).real
Model.sample((a,c)).take(10)

import com.stripe.rainier.notebook._
val ac = Model.sample((a,c))
show("a", "c", scatter(ac))
```


#### eof

