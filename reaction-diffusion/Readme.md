# Stochastic reaction-diffusion modelling (in Scala)

This directory contains the source code associated with the blog post:

[Stochastic reaction-diffusion modelling](DraftPost.md)

This code should run on any system with a recent [Java JDK](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html) installation, and [sbt](https://www.scala-sbt.org/). Note that this code relies on JavaFx libraries, so if you are running OpenJdk, you will also need to install OpenJfx. On most Linux (and similar) systems, this should be as easy as installing the `openjfx` package in addition to (say) `openjdk-8-jdk` using your OS package manager.

Once you have Java and sbt installed, you should be able to compile and run the examples by typing:
```bash
sbt run
```
at your OS prompt from *this* directory (that is, the directory containing the file `build.sbt`). Then just select the number of the example you want to run. The animation should auto-start.



#### eof

