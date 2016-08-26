# R and Java interfacing with rJava and Maven

## Calling Java code from R

This page and the code in this subdirectory serve as an addendum to an old blog post of mine (from 2011) on [Calling Java code from R](https://darrenjw.wordpress.com/2011/01/01/calling-java-code-from-r/). In the post I show how to use the [rJava](https://cran.r-project.org/web/packages/rJava/) package in R to call a Gibbs sampler written in Java. The example Gibbs sampler in Java had an external dependency on the Java-based scientific library [parallel COLT](https://sites.google.com/site/piotrwendykier/software/parallelcolt). In the post I just assumed that parallel COLT was in the users Java classpath, and then everything shoud have worked fine. However, reading through the comments on the post, it is clear that many people have had trouble in getting the example to work due to Java classpath issues.

The example in the post was illustrative, and isn't really a good solution for larger, more complex projects with many dependencies. The "correct" solution to managing Java dependencies is to use a good build tool which understands the dependencies and can manage them for you. In the Java world, the most widely used decent build tool is [Maven](https://maven.apache.org/), so here I show how to use Maven to solve the problem properly.

For this example to work, you must first install Java, Maven, R, and the rJava package. So, `java -version` and `mvn --version` should return something sensible, and entering `library(rJava)` at your R command prompt should return silently (without error). If you have done that, then simply running the script [`./compile-and-run`](compile-and-run) in this directory should compile the Java code and run an R script which calls it. That's it! Look at the scripts and the code to figure out how it all works.

In summary, Maven builds a "fat jar" containing the compiled classes including all dependencies. The magic is all in the Maven config file, [`pom.xml`](pom.xml). It lists the parallel COLT dependency, and uses the "shade plugin" to build the fat jar. So `mvn clean compile package` will build the fat jar without any additional fuss. Then from the R script [`run-gibbs.R`](run-gibbs.R) you just pass a link to the jar when you call `.jinit()` and that's it.

 
