# Stochastic Modelling for Systems Biology, third edition

The third edition of my textbook, [Stochastic Modelling for Systems Biology](https://github.com/darrenjw/smfsb) has recently been published by Chapman & Hall/CRC Press. The book has ISBN-10 **113854928-2** and ISBN-13 **978-113854928-9**. It can be ordered from [CRC Press](https://www.crcpress.com/Stochastic-Modelling-for-Systems-Biology-Third-Edition/Wilkinson/p/book/9781138549289), [Amazon.com](https://amzn.to/2LAVSSN), [Amazon.co.uk](https://amzn.to/2PeDIZt) and similar book sellers.

I was fairly happy with the way that the second edition, published in 2011, turned out, and so I haven't substantially re-written any of the text for the third edition. Instead, I've concentrated on adding in new material and improving the associated on-line resources. Those on-line resources are all free and open source, and hence available to everyone, irrespective of whether you have a copy of the new edition. I'll give an introduction to those resources below (and in subsequent posts). The new material can be briefly summarised as follows:

* New chapter on spatially extended systems, covering the spatial Gillespie algorithm for reaction diffusion master equation (RDME) models in 1- and 2-d, the next subvolume method, spatial CLE, scaling issues, etc.
* Significantly expanded chapter on inference for stochastic kinetic models from data, covering approximate methods of inference (ABC), including ABC-SMC. The material relating to particle MCMC has also been improved and extended.
* Updated R package, including code relating to all of the new material
* New R package for parsing SBML models into simulatable stochastic Petri net models
* New software library, written in Scala, replicating most of the functionality of the R packages in a fast, compiled, strongly typed, functional language

## New content

Although some minor edits and improvements have been made throughout the text, there are two substantial new additions to the text in this new edition. The first is an entirely new chapter on spatially extended systems. The first two editions of the text focused on the implications of discreteness and stochasticity in chemical reaction systems, but maintained the well-mixed assumption throughout. This is a reasonable first approach, since discreteness and stochasticity are most pronounced in very small volumes where diffusion should be rapid. In any case, even these non-spatial models have very interesting behaviour, and become computationally challenging very quickly for non-trivial reaction networks. However, we know that, in fact, the cell is a very crowded environment, and so even at small spatial scales, many interesting processes are diffusion limited. It therefore seems appropriate to dedicate one chapter (the new Chapter 9) to studying some of the implications of relaxing the well-mixed assumption. Entire books can be written on stochastic reaction-diffusion systems, so here only a brief introduction is provided, based mainly around models in the reaction-diffusion master equation (RDME) style. Exact stochastic simulation algorithms are discussed, and implementations provided in the 1- and 2-d cases, and an appropriate Langevin approximation is examined, the spatial CLE.

The second major addition is to the chapter on inference for stochastic kinetic models from data (now Chapter 11). The second edition of the book included a discussion of "likelihood free" Bayesian MCMC methods for inference, and provided a working implementation of likelihood free particle marginal Metropolis-Hastings (PMMH) for stochastic kinetic models. The third edition improves on that implementation, and discusses approximate Bayesian computation (ABC) as an alternative to MCMC for likelihood free inference. Implementation issues are discussed, and sequential ABC approaches are examined, concentrating in particular on the method known as ABC-SMC.

## New software and on-line resources

Accompanying the text are new and improved on-line resources, all well-documented, free, and open source.

### New website/GitHub repo

Information and materials relating to the previous editions were kept on my University website. All materials relating to this new edition are kept in a public GitHub repo: [darrenjw/smfsb](https://github.com/darrenjw/smfsb). This will be simpler to maintain, and will make it much easier for people to make copies of the material for use and studying off-line.

### Updated R package(s)

Along with the second edition of the book I released an accompanying R package, "smfsb", published on CRAN. This was a very popular feature, allowing anyone with R to trivially experiment with all of the models and algorithms discussed in the text. This R package has been updated, and a new version has been published to CRAN. The updates are all backwards-compatible with the version associated with the second edition of the text, so owners of that edition can still upgrade safely. I'll give a proper introduction to the package, including the new features, in a subsequent post, but in the meantime, you can install/upgrade the package from a running R session with
```R
install.packages("smfsb")
```
and then pop up a tutorial vignette with:
```R
vignette("smfsb")
```
This should be enough to get you started.

In addition to the main R package, there is an additional R package for parsing SBML models into models that can be simulated within R. This package is not on CRAN, due to its dependency on a non-CRAN package. See the [repo](https://github.com/darrenjw/smfsb) for further details.

There are also Python scripts available for converting SBML models to and from the shorthand SBML notation used in the text.

### New Scala library

Another major new resource associated with the third edition of the text is a software library written in the Scala programming language. This library provides Scala implementations of all of the algorithms discussed in the book and implemented in the associated R packages. This then provides example implementations in a fast, efficient, compiled language, and is likely to be most useful for people wanting to use the methods in the book for research. Again, I'll provide a tutorial introduction to this library in a subsequent post, but it is well-documented, with all necessary information needed to get started available at the [scala-smfsb](https://github.com/darrenjw/scala-smfsb) repo/website.


#### eof
