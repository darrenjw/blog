# Working with SBML using Scala

*This is a draft of a post that will go on my blog. I'll put a link to it from here after posting. This draft is not the definitive version.*

## Introduction

The <a href="http://sbml.org/">Systems Biology Markup Language (SBML)</a> is an <a href="https://en.wikipedia.org/wiki/XML">XML</a>-based format for representation and exchange of biochemical network models. SBML is supported by most systems biology modelling tools, allowing the export of a model in SBML from one tool and then reading in another tool. Because it offers a standard way of representing biochemical networks in an unambiguous way, it can also be used as the standard way of representing models in databases of biochemical network models, such as <a href="https://www.ebi.ac.uk/biomodels-main/">BioModels</a>. I haven't talked about SBML much in this blog, so far, but I discuss it in detail in my book, <a href="http://www.staff.ncl.ac.uk/d.j.wilkinson/smfsb/">Stochastic modelling for systems biology</a>. SBML is a "good thing", and everyone who works with (deterministic or stochastic) biochemical network models should know a bit about it.

The SBML format is fairly complex to parse and generate correctly, so it's preferable to use a software library to take care of the details. <a href="http://sbml.org/Software/libSBML">libSBML</a> is the community standard library developed for this purpose. It is a C++ library, but has interfaces for other languages, such as Python and Java. However, whilst it's perfectly possible to use native libraries on the <a href="https://en.wikipedia.org/wiki/Java_virtual_machine">JVM</a>, they aren't so convenient to work with, especially in conjunction with modern automatic build and deployment tools. So when working on the JVM, a pure JVM library for working with SBML would be a lot more convenient. <a href="http://sbml.org/Software/JSBML">JSBML</a> is exactly that - a pure Java library for working with SBML on the JVM. As of version 1.2, it is also available from <a href="http://search.maven.org/">Maven Central</a>, making it super-convenient to use with modern build tools such as <a href="https://maven.apache.org/">Maven</a> and <a href="http://www.scala-sbt.org/">sbt</a>. In this post I'll walk through getting started with using <a href="https://www.scala-lang.org/">Scala</a> and sbt to build and run a trivial JSBML example, and highlight a couple of gotchas and provide pointers for further reading. 

## Using JSBML from Scala sbt projects

Since JSBML is now on Maven Central, adding a dependency on it should just be a matter of adding the line
```scala
libraryDependencies += "org.sbml.jsbml" % "jsbml" % "1.2"
```
to your sbt `build.sbt` file. However, for slightly mysterious reasons this doesn't quite work. It works fine for compilation, but at runtime some dependencies are missing. I suspect this is a slight problem with the current JSBML build, but it could also be a bug/feature in sbt. Either way, the problem can be solved by explicitly including <a href="http://logging.apache.org/log4j/2.x/">log4j</a> dependencies in the build. So just adding:
```scala
libraryDependencies ++= Seq(
		"org.sbml.jsbml" % "jsbml" % "1.2",
		"org.apache.logging.log4j" % "log4j-1.2-api" % "2.3",
		"org.apache.logging.log4j" % "log4j-api" % "2.3",
		"org.apache.logging.log4j" % "log4j-core" % "2.3"
			)
```
to the build file is sufficient to make everything work properly.

**Example**

**Discussion of Unit**

**Further reading**

This complete runnable example is available in my <a href="https://github.com/darrenjw/blog/tree/master/sbml-scala">blog repo</a> on github. This example will run on any system with a recent JVM installed. It does not require Scala, or libSBML, or JSBML, or any other dependency. 

## Conclusion

**Brief wrap-up**







