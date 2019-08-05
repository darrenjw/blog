# Write your own general purpose monadic probabilistic programming language from scratch in 50 lines of (Scala) code

```tut
1+2
```

```tut:silent
1+2
```

```tut:book
1+2
```

## Background

In May I attended a great workshop on [advances and challenges in machine learning languages
](https://yebai.github.io/mll-workshop/) at the CMS in Cambridge. There was an a good mix of people from different disciplines, and a bit of a theme around probabilistic programming. The [workshop schedule](https://yebai.github.io/mll-workshop/schedule.html) includes links to many of the presentations, and is generally worth browsing. In particular, it includes a link to the slides for my presentation on [a compositional approach to scalable Bayesian computation and probabilistic programming](https://drive.google.com/file/d/1c8o_K187x9QpKB1p2QFu7VRAnMiskcYU/view?usp=sharing). I've given a few talks on this kind of thing over the last couple of years, at Newcastle, at the Isaac Newton Institute in Cambridge (twice), and at CIRM in France. But I think I explained things best at this workshop at the CMS, though my impression could partly have been a reflection of the more interested and relevant audience. In the talk I started with a basic explanation of why ideas from [category theory](https://en.wikipedia.org/wiki/Category_theory) and [functional programming](https://en.wikipedia.org/wiki/Functional_programming) can help to solve problems in statistical computing in a more composable and scalable way, before moving on to discuss probability monads and their fundamental connection to probabilistic programming. The take home message from the talk is that if you have a generic inference algorithm, expressing the logic in the context of a probability monads can give you an embedded probabilistic programming language (PPL) for that inference algorithm essentially "for free".

So, during my talk I said something a little foolhardy. I can't remember my exact words, but while presenting the idea behind an [SMC](https://en.wikipedia.org/wiki/Particle_filter)-based probability monad I said something along the lines of "*one day I will write a blog post on how to write a probabilistic programming language from scratch in 50 lines of code, and this is how I'll do it*"! Rather predictably (with hindsight), immediately after my talk about half a dozen people all pleaded with me to urgently write the post! I've been a little busy since then, but now that things have settled down a little for the summer, I've some time to think and code, so here is that post.

## Introduction

The idea behind this post is to show that, if you think about the problem in the right way, and use a programming language with syntactic support for monadic compostion, then producing a flexible, general, compositional, embedded [domain specific language](https://en.wikipedia.org/wiki/Domain-specific_language) (DSL) for [probabilistic programming](https://en.wikipedia.org/wiki/Probabilistic_programming) based on a given generic inference algorithm is no more effort than hard-coding two or three illustrative examples. You would need to code up two or three examples for a paper anyway, but providing a PPL is *way* more useful. There is also an interesting converse to this, which is that if you *can't* easily produce a PPL for your "general" inference algorithm, then perhaps it isn't quite as "general" as you thought. I'll try to resist exploring that here...

To illustrate these principles I wan't to develop a fairly *minimal* PPL, so that the complexities of the inference algorithm don't hide the simplicity of the PPL embedding. [Importance sampling](https://en.wikipedia.org/wiki/Importance_sampling) with resampling is probably the simplest useful generic Bayesian inference algorithm to implement, so that's what I'll use. Note that there are many limitations of the approach that I will adopt, which will make it completely unsuitable for "real" problems. In particular, this implementation is: inefficient, in terms of both compute time and memory usage, statistically inefficient for deep nesting due to the particle degeneracy problem, specific to a particular probability monad, strictly evaluated, impure (due to mutation of global random number state), etc. All of these things are easily fixed, but all at the expense of greater abstraction, complexity and lines of code. I'll probably discuss some of these generalisations and improvements in future posts, but for this post I want to keep everything as short and simple as practical. It's also worth mentioning that there is nothing particularly original here. Lot's of people have written about monadic embedded PPLs, and several have used an SMC-based monad for illustration. I'll give some pointers to useful further reading at the end.





