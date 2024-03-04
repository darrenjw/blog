# MCMC code for Bayesian inference for a discretely observed stochastic kinetic model

In June this year the (twice COVID-delayed) [Richard J Boys Memorial Workshop](https://conferences.ncl.ac.uk/rjbmemorialmeeting/) finally took place, celebrating the life and work of my former colleague and collaborator, who died suddenly in 2019 ([obituary](http://www.bernoulli-society.org/files/BernoulliNews2019-1.pdf#page=13)). I completed the programme of talks by delivering the inaugural RSS North East Richard Boys lecture. For this, I decided that it would be most appropriate to talk about the paper [Bayesian inference for a discretely observed stochastic kinetic model](http://dx.doi.org/10.1007/s11222-007-9043-x), published in Statistics and Computing in 2008. The paper is concerned with (exact and approximate) MCMC-based fully Bayesian inference for continuous time Markov jump processes observed partially and discretely in time. Although the ideas are generally applicable to a broad class of "stochastic kinetic models", the running example throughout the paper is a discrete stochastic Lotka Volterra model.

In preparation for the talk, I managed to track down most of the MCMC codes used for the examples presented in the paper. This included C code I wrote for exact and approximate block-updating algorithms, and Fortran code written by Richard using an exact reversible jump MCMC approach. I've fixed up all of the codes so that they are now easy to build and run on a modern Linux (or other Unix-like) system, and provided some minimal documentation. It is all available in a [public github repo](https://github.com/darrenjw/BWK). Hopefully this will be of some interest or use to a few people.