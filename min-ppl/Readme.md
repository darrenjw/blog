# A mimimal probabilistic programming language (PPL) in Scala

Resources for a blog post on writing a minimal PPL:

## [Write your own general-purpose monadic probabilistic programming language from scratch in 50 lines of (Scala) code](https://darrenjw.wordpress.com/2019/08/07/write-your-own-general-purpose-monadic-probabilistic-programming-language-from-scratch-in-50-lines-of-scala-code/)

If you have (a recent JDK, and) [sbt](https://www.scala-sbt.org/) installed, you can compile and run the examples with `sbt run`, or run some tests with `sbt test` (slow), or compile the [tut](http://tpolecat.github.io/tut/) document that formed the draft of the post with `sbt tut`.

Depending on your machine, JDK version, `sbt` version, etc., it may be helpful to start up the JVM with a bigger heap. eg. `sbt -mem 8000 test` might start up the JVM with an 8GB heap to run the tests, and that might be helpful. YMMV.

If you are a statistician or data scientist interested to learn more about Scala, note that I have a free on-line course available: [Scala for statistical computing and data science](https://github.com/darrenjw/scala-course/blob/master/StartHere.md)


Copyright (C) 2019 [Darren J Wilkinson](https://darrenjw.github.io/)

