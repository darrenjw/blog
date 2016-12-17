# Working with SBML using Scala

## Introduction

The <a href="http://sbml.org/">Systems Biology Markup Language (SBML)</a> is an <a href="https://en.wikipedia.org/wiki/XML">XML</a>-based format for representation and exchange of biochemical network models. SBML is supported by most systems biology modelling tools, allowing the export of a model in SBML from one tool and then reading in another tool. Because it offers a standard way of representing biochemical networks in an unambiguous way, it can also be used as the standard way of representing models in databases of biochemical network models, such as <a href="https://www.ebi.ac.uk/biomodels-main/">BioModels</a>. I haven't talked about SBML much in this blog, so far, but I discuss it in detail in my book, <a href="http://www.staff.ncl.ac.uk/d.j.wilkinson/smfsb/">Stochastic modelling for systems biology</a>. SBML is a "good thing", and everyone who works with (deterministic or stochastic) biochemical network models should know a bit about it.

The SBML format is fairly complex to parse and generate correctly, so it's preferable to use a software library to take care of the details. <a href="http://sbml.org/Software/libSBML">libSBML</a> is the community standard library developed for this purpose. It is a C++ library, but has interfaces for other languages, such as Python and Java. However, whilst it's perfectly possible to use native libraries on the <a href="https://en.wikipedia.org/wiki/Java_virtual_machine">JVM</a>, they aren't so convenient to work with, especially in conjunction with modern automatic build and deployment tools. So when working on the JVM, a pure JVM library for working with SBML would be a lot more convenient. <a href="http://sbml.org/Software/JSBML">JSBML</a> is exactly that - a pure Java library for working with SBML on the JVM. As of version 1.2, it is also available from <a href="http://search.maven.org/">Maven Central</a>, making it super-convenient to use with modern build tools such as <a href="https://maven.apache.org/">Maven</a> and <a href="http://www.scala-sbt.org/">sbt</a>. In this post I'll walk through getting started with using <a href="https://www.scala-lang.org/">Scala</a> and sbt to build and run a trivial JSBML example, and highlight a couple of gotchas and provide pointers for further reading. 

## Using JSBML from Scala sbt projects



## Conclusion









