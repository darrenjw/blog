# A probability monad for the bootstrap partcile filter

## Introduction

In the previous post I showed how to [write your own general-purpose monadic probabilistic programming language from scratch in 50 lines of (Scala) code](https://darrenjw.wordpress.com/2019/08/07/write-your-own-general-purpose-monadic-probabilistic-programming-language-from-scratch-in-50-lines-of-scala-code/). In that post I tried to keep everything as simple as possible, but at the expense of both elegance and efficiency. In this post I'll address one problem with the implementation from that post - the memory (and computational) overhead associated with forming the cartesian product of particle sets during monadic binding (`flatMap`). So if particle sets are typically of size $N$, then the cartesian product is of size $N^2$, and multinomial resampling is applied to this set of size $N^2$ in order to sample back down to a set of size $N$. But this isn't actually necessary. We can directly construct a set of size $N$, certainly saving memory, but also potentially saving computation time if the conditional distribution (on the right of the monadic bind) can be efficiently sampled. If we do this we will have a probability monad encapsulating the logic of a bootstrap [particle filter](https://en.wikipedia.org/wiki/Particle_filter), such as is often used for computing the filtering distribution of a state-space model in time series analysis. This simple change won't solve the computational issues associated with deep monadic binding, but does solve the memory problem, and can lead to computationally efficient algorithms so long as care is taken in the formulation of probabilistic programs to ensure that deep monadic binding doesn't occur. We'll discuss that issue in the context of state-space models later, once we have our new SMC-based probability monad.




## Summary and conclusions

### Software

* [min-ppl](https://github.com/darrenjw/blog/tree/master/min-ppl) - code associated with this blog post
* [Rainier](https://github.com/stripe/rainier) - a more efficient PPL with similar syntax
* [monad-bayes](https://github.com/adscib/monad-bayes) - a Haskell library exploring related ideas



