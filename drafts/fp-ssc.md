## An introduction to functional programming for scalable statistical computing and machine learning

Functional programming (FP) languages are great for statistical computing, computational statistics, and machine learning. They are particularly well-suited to scalable computation, where this could either mean scaling up to distributed algorithms for very big data, or running algorithms for more moderately sized data sets very fast in parallel on GPUs. However, people unfamiliar with FP often find FP languages quite intimidating, due to the fairly steep initial learning curve. This issue is exacerbated by the fact that there is very little introductory documentation available for people new to FP who are interested in applications to statistical computing and machine learning (ML).

So for some time I've been meaning to put together materials for a short course (suitable for self-study) that will get people started with FP in few different languages, with a very basic problem from statistical computing used as the running example, together with a catalogue of resources for further learning, in order to provide people with the information they need to keep going once they have got over the initial hurdle. But as with many things, it never got high enough up my priority list to actually sit down and do it. Fortunately, [StatML](https://statml.io/) invited me to deliver some training in advanced statistical computing, so this gave me the perfect motivation to actually assemble something. The in-person training has been delayed (due to the [UCU](https://www.ucu.org.uk/) strike), but the materials are all prepared and publicly available, and suitable for self-study now.

The course gives a very quick introduction to the ideas of FP, followed by very quick hands-on introductions to my favourite FP languages/libraries: Scala, Haskell, JAX and Dex. There is also a brief introduction to *splittable random number generators* which are becoming increasingly popular for the development of functional parallel Monte Carlo algorithms.

If you've been vaguely interested in FP for statistical computing and ML but not sure how to get started, hopefully this solves the problem.

[An introduction to functional programming for scalable statistical computing and machine learning](https://github.com/darrenjw/fp-ssc-course) (short course)

