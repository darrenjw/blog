# Scala for Data Science [book review]

*This is the draft of my blog post: https://darrenjw.wordpress.com/2016/12/22/scala-for-data-science-book-review/ - This is not the definitive version.*

This post will review the book:

* [Scala for Data Science](http://amzn.to/2hKGIz2), Bugnion, Packt, 2016.

*Disclaimer: This book review has not been solicited by the publisher (or anyone else) in any way. I purchased the review copy of this book myself. I have not received any benefit from the writing of this review.*

## Introduction

On this blog I previously reviewed the (terrible) book, [Scala for machine learning](https://darrenjw.wordpress.com/2015/04/09/scala-for-machine-learning-book-review/) by the same publisher. I was therefore rather wary of buying this book. But the topic coverage looked good, so I decided to buy it, and wasn't disappointed. Scala for Data Science is my top recommendation for getting started with statistical computing and data science applications using Scala.

## Overview

The book assumes a basic familiarity with programming in Scala, at around the level of someone who has completed the [Functional Programming Principles in Scala](https://www.coursera.org/learn/progfun1) Coursera course. That is, it (quite sensibly) doesn't attempt to teach the reader how to program in Scala, but rather how to approach the development of data science applications using Scala. It introduces more advanced Scala idioms gradually (eg. typeclasses don't appear until Chapter 5), so it is relatively approachable for those who aren't yet Scala experts. The book does cover [Apache Spark](http://spark.apache.org/), but Spark isn't introduced until Chapter 10, so it isn't "just another Spark book". Most of the book is about developing data science applications in Scala, completely independently of Spark. That said, it also provides one of the better introductions to Spark, so doubles up as a pretty good introductory Spark book, in addition to being a good introduction to the development of data science applications with Scala. It should probably be emphasised that the book is very much focused on data science, rather than statistical computing, but there is plenty of material of relevance to those who are more interested in statistical computing than applied data science.


## Chapter by chapter

1. *Scala and Data Science* - motivation for using Scala in preference to certain other languages I could mention...
2. *Manipulating data with Breeze* - [Breeze](https://github.com/scalanlp/breeze) is the standard Scala library for scientific and statistical computing. It's pretty good, but documentation is rather lacking. This Chapter provides a good tutorial introduction to Breeze, which should be enough to get people going sufficiently to be able to make some sense of the available on-line documentation.
3. *Plotting with breeze-viz* - Breeze has some support for plotting and visualisation of data. It's somewhat limited when compared to what is available in R, but is fine for interactive exploratory analysis. However, the available on-line documentation for breeze-viz is almost non-existent. This Chapter is the best introduction to breeze-viz that I have seen.
4. *Parallel collections and futures* - the Scala standard library has built-in support for parallel and concurrent programming based on functional programming concepts such as parallel (monadic) collections and Futures. Again, this Chapter provides an excellent introduction to these powerful concepts, allowing the reader to start developing parallel algorithms for multi-core hardware with minimal fuss.
5. *Scala and SQL through JDBC* - this Chapter looks at connecting to databases using standard JVM mechanisms such as JDBC. However, it gradually introduces more functional ways of interfacing with databases using typeclasses, motivating:
6. *Slick - a functional interface for SQL* - an introduction to the Slick library for a more Scala-esque way of database interfacing.
7. *Web APIs* - the practicalities of talking to web APIs. eg. authenticated HTTP requests and parsing of JSON responses.
8. *Scala and MongoDB* - working with a NoSQL database from Scala
9. *Concurrency with Akka* - Akka is the canonical implementation of the actor model in Scala, for building large concurrent applications. It is the foundation on which Spark is built.
10. *Distributed batch processing with Spark* - a tutorial introduction to Apache Spark. Spark is a big data analytics framework built on top of Scala and Akka. It is arguably the best available framework for big data analytics on computing clusters in the cloud, and hence there is a lot of interest in it. Indeed, Spark is driving some of the interest in Scala.
11. *Spark SQL and DataFrames* - interfacing with databases using Spark, and more importantly, an introduction to Spark's DataFrame abstraction, which is now fundamental to developing machine learning pipelines in Spark.
12. *Distributed machine learning with MLLib* - MLLib is the machine learning library for Spark. It is worth emphasising that unlike many early books on Spark, this chapter covers the newer DataFrame-based pipeline API, in addition to the original RDD-based API. Together, Chapters 11 and 12 provide a pretty good tutorial introduction to Spark. After working through these, it should be easy to engage with the official on-line Spark documentation.
13. *Web APIs with Play* - is concerned with developing a web API at the end of a data science pipeline.
14. *Visualisation with D3 and the Play framework* - is concerned with integrating visualisation into a data science web application.

## Summary

This book provides a good tutorial introduction to a large number of topics relevant to statisticians and data scientists interested in developing data science applications using Scala. After working through this book, readers should be well-placed to augment their knowledge with readily searchable on-line documentation.

In a follow-up post I will give a quick overview of some other books relevant to getting started with Scala for statistical computing and data science. 




