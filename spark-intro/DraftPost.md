# A quick introduction to Apache Spark for statisticians

## Draft Post - this is NOT the definitive version

The final version of this post is now published at: https://darrenjw.wordpress.com/2017/02/08/a-quick-introduction-to-apache-spark-for-statisticians/

## Introduction

[Apache Spark](http://spark.apache.org/) is a Scala library for analysing "big data". It can be used for analysing huge (internet-scale) datasets distributed across large clusters of machines. The analysis can be anything from the computation of simple descriptive statistics associated with the datasets, through to rather sophisticated machine learning pipelines involving data pre-processing, transformation, nonlinear model fitting and regularisation parameter tuning (via methods such as cross-validation). A relatively impartial overview can be found in the [Apache Spark Wikipedia page](https://en.wikipedia.org/wiki/Apache_Spark).

Although Spark is really aimed at data that can't easily be analysed on a laptop, it is actually very easy to install and use (in [standalone mode](http://spark.apache.org/docs/latest/spark-standalone.html)) on a laptop, and a good laptop with a fast multicore processor and plenty of RAM is fine for datasets up to a few gigabytes in size. This post will walk through getting started with Spark, installing it locally (not requiring admin/root access) doing some simple descriptive analysis, and moving on to fit a simple linear regression model to some simulated data. After this walk-through it should be relatively easy to take things further by reading the [Spark documentation](http://spark.apache.org/docs/latest/), which is generally pretty good.

Anyone who is interested in learning more about setting up and using Spark clusters may want to have a quick look over on my [personal blog](https://darrenjw2.wordpress.com/) (mainly concerned with the Raspberry Pi), where I have previously considered [installing Spark on a Raspberry Pi 2](https://darrenjw2.wordpress.com/2015/04/17/installing-apache-spark-on-a-raspberry-pi-2/), [setting up a small Spark cluster](https://darrenjw2.wordpress.com/2015/04/18/setting-up-a-standalone-apache-spark-cluster-of-raspberry-pi-2/), and [setting up a larger Spark cluster](https://darrenjw2.wordpress.com/2015/09/07/raspberry-pi-2-cluster-with-nat-routing/). Although these posts are based around the Raspberry Pi, most of the material there is quite generic, since the Raspberry Pi is just a small (Debian-based) Linux server.

## Getting started - installing Spark

The only pre-requisite for installing Spark is a recent Java installation. On Debian-based Linux systems (such as Ubuntu), Java can be installed with:

```bash
sudo apt-get update
sudo apt-get install openjdk-8-jdk
```

For other systems you should Google for the best way to install Java. If you aren't sure whether you have Java or not, type `java -version` into a terminal window. If you get a version number of the form 1.7.x or 1.8.x you should be fine.

Once you have Java installed, you can download and install Spark in any appropriate place in your file-system. If you are running Linux, or a Unix-alike, just `cd` to an appropriate place and enter the following commands:

```bash
wget http://www.eu.apache.org/dist/spark/spark-2.1.0/spark-2.1.0-bin-hadoop2.7.tgz
tar xvfz spark-2.1.0-bin-hadoop2.7.tgz 
cd spark-2.1.0-bin-hadoop2.7
bin/run-example SparkPi 10
```

If all goes well, the last command should run an example. Don't worry if there are lots of INFO and WARN messages - we will sort that out shortly. On other systems it should simply be a matter of downloading and unpacking Spark somewhere appropriate, then running the example from the top-level Spark directory. Get Spark from the [downloads page](http://spark.apache.org/downloads.html). You should get version 2.1.0 built for Hadoop 2.7. It doesn't matter if you don't have Hadoop installed - it is not required for single-machine use.

The INFO messages are useful for debugging cluster installations, but are too verbose for general use. On a Linux system you can turn down the verbosity with:

```bash
sed 's/rootCategory=INFO/rootCategory=WARN/g' < conf/log4j.properties.template > conf/log4j.properties
```

On other systems, copy the file `log4j.properties.template` in the `conf` sub-directory to `log4j.properties` and edit the file, replacing `INFO` with `WARN` on the relevant line. Check it has worked by re-running the `SparkPi` example - it should be much less verbose this time. You can also try some other examples:

```bash
bin/run-example SparkLR
ls examples/src/main/scala/org/apache/spark/examples/
```

There are several different ways to use Spark. For this walk-through we are just going to use it interactively from the "Spark shell". We can pop up a shell with:

```bash
bin/spark-shell --master local[4]
```

The "4" refers to the number of worker threads to use. Four is probably fine for most decent laptops. `Ctrl-D` or `:quit` will exit the Spark shell and take you back to your OS shell. It is more convenient to have the Spark `bin` directory in your path. If you are using `bash` or a similar OS shell, you can temporarily add the Spark `bin` to your path with the OS shell command:

```bash
export PATH=$PATH:`pwd`/bin
```

You can make this permanent by adding a line like this (but with the full path hard-coded) to your `.profile` or similar start-up dot-file. I prefer not to do this, as I typically have several different Spark versions on my laptop and want to be able to select exactly the version I need. If you are not running `bash`, Google how to add a directory to your path. Check the path update has worked by starting up a shell with:

```bash
spark-shell --master local[4]
```

Note that if you want to run a script containing Spark commands to be run in "batch mode", you could do it with a command like:

```bash
spark-shell --driver-memory 25g --master local[4] < spark-script.scala | tee script-out.txt
```

There are much better ways to develop and submit batch jobs to Spark clusters, but I won't discuss those in this post. Note that while Spark is running, diagnostic information about the "cluster" can be obtained by pointing a web browser at port 4040 on the master, which here is just http://localhost:4040/ - this is extremely useful for debugging purposes.

## First Spark shell commands

### Counting lines in a file

We are now ready to start using Spark. From a Spark shell in the top-level directory, enter:

```scala
sc.textFile("README.md").count
```

If all goes well, you should get a count of the number of lines in the file `README.md`. The value `sc` is the "Spark context", containing information about the Spark cluster (here it is just a laptop, but in general it could be a large cluster of machines, each with many processors and each processor with many cores). The `textFile` method loads up the file into an RDD (Resilient Distributed Dataset). The RDD is the fundamental abstraction provided by Spark. It is a lazy distributed parallel monadic collection. After loading a text file like this, each element of the collection represents one line of the file. I've talked about monadic collections in previous posts, so if this isn't a familiar concept, it might be worth having a quick skim through at least the post on [first steps with monads in Scala](https://darrenjw.wordpress.com/2016/04/15/first-steps-with-monads-in-scala/). The point is that although RDDs are potentially huge and distributed over a large cluster, using them is very similar to using any other monadic collection in Scala. We can unpack the previous command slightly as follows:

```scala
val rdd1 = sc.textFile("README.md")
rdd1
rdd1.count
```

Note that RDDs are "lazy", and this is important for optimising complex pipelines. So here, after assigning the value `rdd1`, no data is actually loaded into memory. All of the actual computation is deferred until an "action" is called - `count` is an example of such an action, and therefore triggers the loading of data into memory and the counting of elements.

### Counting words in a file

We can now look at a very slightly more complex pipeline - counting the number of words in a text file rather than the number of lines. This can be done as follows:

```scala
sc.textFile("README.md").
  map(_.trim).
  flatMap(_.split(' ')).
  count
```

Note that `map` and `flatMap` are both lazy ("transformations" in Spark terminology), and so no computation is triggered until the final action, `count` is called. The call to `map` will just trim any redundant white-space from the line ends. So after the call to `map` the RDD will still have one element for each line of the file. However, the call to `flatMap` splits each line on white-space, so after this call each element of the RDD will correspond to a word, and not a line. So, the final `count` will again count the number of elements in the RDD, but here this corresponds to the number of words in the file.

### Counting character frequencies in a file

A final example before moving on to look at quantitative data analysis: counting the frequency with which each character occurs in a file. This can be done as follows:

```scala
sc.textFile("README.md").
  map(_.toLowerCase).
  flatMap(_.toCharArray).
  map{(_,1)}.
  reduceByKey(_+_).
  collect
```

The first call to `map` converts upper case characters to lower case, as we don't want separate counts for upper and lower case characters. The call to `flatMap` then makes each element of the RDD correspond to a single character in the file. The second call to `map` transforms each element of the RDD to a key-value pair, where the key is the character and the value is the integer 1. RDDs have special methods for key-value pairs in this form - the method `reduceByKey` is one such - it applies the reduction operation (here just "+") to all values corresponding to a particular value of the key. Since each character has the value 1, the sum of the values will be a character count. Note that the reduction will be done in parallel, and for this to work it is vital that the reduction operation is associative. Simple addition of integers is clearly associative, so here we are fine. Note that `reduceByKey` is a (lazy) transformation, and so the computation needs to be triggered by a call to the action `collect`.

On most Unix-like systems there is a file called `words` that is used for spell-checking. The example below applies the character count to this file. Note the calls to `filter`, which filter out any elements of the RDD not matching the predicate. Here it is used to filter out special characters.

```scala
sc.textFile("/usr/share/dict/words").
  map(_.trim).
  map(_.toLowerCase).
  flatMap(_.toCharArray).
  filter(_ > '/').
  filter(_ < '}').
  map{(_,1)}.
  reduceByKey(_+_).
  collect
```

## Analysis of quantitative data

### Descriptive statistics

We first need some quantitative data, so let's simulate some. [Breeze](https://github.com/scalanlp/breeze/) is the standard Scala library for scientific and statistical computing. I've given a [quick introduction to Breeze](https://darrenjw.wordpress.com/2013/12/30/brief-introduction-to-scala-and-breeze-for-statistical-computing/) in a previous post. Spark has a dependence on Breeze, and therefore can be used from inside the Spark shell - this is very useful. So, we start by using Breeze to simulate a vector of normal random quantities:

```scala
import breeze.stats.distributions._
val x = Gaussian(1.0,2.0).sample(10000)
```

Note, though, that `x` is just a regular Breeze Vector, a simple serial collection all stored in RAM on the master thread. To use it as a Spark RDD, we must convert it to one, using the `parallelize` function:

```scala
val xRdd = sc.parallelize(x)
```

Now `xRdd` is an RDD, and so we can do Spark transformations and actions on it. There are some special methods for RDDs containing numeric values:

```scala
xRdd.mean
xRdd.sampleVariance
```

Each summary statistic is computed with a single pass through the data, but if several summary statistics are required, it is inefficient to make a separate pass through the data for each summary, so the `stats` method makes a single pass through the data returning a `StatsCounter` object that can be used to compute various summary statistics.

```scala
val xStats = xRdd.stats
xStats.mean
xStats.sampleVariance
xStats.sum
```

The `StatsCounter` methods are: `count`, `mean`, `sum`, `max`, `min`, `variance`, `sampleVariance`, `stdev`, `sampleStdev`.

### Linear regression

Moving beyond very simple descriptive statistics, we will look at a simple linear regression model, which will also allow us to introduce Spark `DataFrame`s - a high level abstraction layered on top of RDDs which makes working with tabular data much more convenient, especially in the context of statistical modelling.

We start with some standard (non-Spark) Scala Breeze code to simulate some data from a simple linear regression model. We use the `x` already simulated as our first covariate. Then we simulate a second covariate, `x2`. Then, using some residual noise, `eps`, we simulate a regression model scenario, where we know that the "true" intercept is 1.5 and the "true" covariate regression coefficients are 2.0 and 1.0. 

```scala
val x2 = Gaussian(0.0,1.0).sample(10000)
val xx = x zip x2
val lp = xx map {p => 2.0*p._1 + 1.0*p._2 + 1.5}
val eps = Gaussian(0.0,1.0).sample(10000)
val y = (lp zip eps) map (p => p._1 + p._2)
val yx = (y zip xx) map (p => (p._1,p._2._1,p._2._2))

val rddLR = sc.parallelize(yx)
```

Note that the last line converts the regular Scala Breeze collection into a Spark RDD using `parallelize`. We could, in principle, do regression modelling using raw RDDs, and early versions of Spark required this. However, statisticians used to statistical languages such as R know that data frames are useful for working with tabular data. I gave a brief overview of [Scala data frame libraries](https://darrenjw.wordpress.com/2015/08/21/data-frames-and-tables-in-scala/) in a previous post. We can convert an RDD of tuples to a Spark `DataFrame` as follows:

```scala
val dfLR = rddLR.toDF("y","x1","x2")
dfLR.show
dfLR.show(5)
```

Note that `show` shows the first few rows of a `DataFrame`, and giving it a numeric argument specifies the number to show. This is very useful for quick sanity-checking of `DataFrame` contents.

Note that there are other ways of getting data into a Spark `DataFrame`. One of the simplest ways to get data into Spark from other systems is via a CSV file. A properly formatted CSV file with a header row can be read into Spark with a command like:

```scala
// val df = spark.read.
//   option("header","true").
//   option("inferSchema","true").
//   csv("myCsvFile.csv")
```

This requires two passes over the data - one to infer the schema and one to actually read the data. For very large datasets it is better to declare the schema and not use automatic schema inference. However, for very large datasets, CSV probably isn't a great choice of format anyway. Spark supports many more efficient data storage formats. Note that Spark also has functions for querying SQL (and other) databases, and reading query results directly into `DataFrame` objects. For people familiar with databases, this is often the most convenient way of ingesting data into Spark. See the Spark [DataFrames guide](http://spark.apache.org/docs/latest/sql-programming-guide.html) and the API docs for [DataFrameReader](http://spark.apache.org/docs/latest/api/scala/#org.apache.spark.sql.DataFrameReader) for further information.

Spark has an extensive library of tools for the development of sophisticated machine learning pipelines. Included in this are functions for fitting linear regression models, regularised regression models (Lasso, ridge, elastic net), generalised linear models, including logistic regression models, etc., and tools for optimising regularisation parameters, for example, using cross-validation. For this post I'm just going to show how to fit a simple OLS linear regression model: see the [ML pipeline documentation](http://spark.apache.org/docs/2.1.0/ml-pipeline.html) for further information, especially the docs on [classification and regression](http://spark.apache.org/docs/2.1.0/ml-classification-regression.html). 

We start by creating an object for fitting linear regression models:

```scala
import org.apache.spark.ml.regression.LinearRegression
import org.apache.spark.ml.linalg._

val lm = new LinearRegression
lm.explainParams
lm.getStandardization
lm.setStandardization(false)
lm.getStandardization
lm.explainParams
```

Note that there are many parameters associated with the fitting algorithm, including regularisation parameters. These are set to defaults corresponding to no regularisation (simple OLS). Note, however, that the algorithm defaults to standardising covariates to be mean zero variance one. We can turn that off before fitting the model if desired.

Also note that the model fitting algorithm assumes that the `DataFrame` to be fit has (at least) two columns, one called `label` containing the response variable, and one called `features`, where each element is actually a `Vectors` of covariates. So we first need to transform our `DataFrame` into the required format.

```scala
// Transform data frame to required format
val dflr = (dfLR map {row => (row.getDouble(0), 
           Vectors.dense(row.getDouble(1),row.getDouble(2)))}).
           toDF("label","features")
dflr.show(5)
```

Now we have the data in the correct format, it is simple to fit the model and look at the estimated parameters.

```scala
// Fit model
val fit = lm.fit(dflr)
fit.intercept
fit.coefficients
```

You should see that the estimated parameters are close to the "true" parameters that were used to simulate from the model. More detailed diagnostics can be obtained from the fitted summary object.

```scala
val summ = fit.summary
summ.r2
summ.rootMeanSquaredError
summ.coefficientStandardErrors
summ.pValues
summ.tValues
summ.predictions
summ.residuals
```

So, that's how to fit a simple OLS linear regression model. Fitting GLMs (including logistic regression) is very similar, and setting up routines to tune regularisation parameters via cross-validation is not much more difficult.

## Further reading

As previously mentioned, once you are up and running with a Spark shell, the official Spark documentation is reasonably good. First go through the [quick start guide](http://spark.apache.org/docs/latest/quick-start.html), then the [programming guide](http://spark.apache.org/docs/latest/programming-guide.html), then the [ML guide](http://spark.apache.org/docs/latest/ml-guide.html), and finally, consult the [API docs](http://spark.apache.org/docs/latest/api/scala/). I discussed [books on scala for data science](https://darrenjw.wordpress.com/2016/12/22/books-on-scala-for-statistical-computing-and-data-science/) in the previous post - many of these cover Spark to a greater or lesser extent.

I recently gave a talk on some of the general principles behind the use of functional programming for scalable statistical computing, and how concepts from category theory, such as monads, can help. The [PDF slides](http://www.mas.ncl.ac.uk/~ndjw1/docs/djw-ctfp.pdf) are available. I'm not sure how comprehensible they will be without my explanations and white-board diagrams, but come to think of it, I'm not sure how comprehensible they were *with* my explanations and white-board diagrams... Also note that I occasionally run a three-day short-course on [Scala for statistical computing](https://github.com/darrenjw/scala-course/blob/master/README.md), and much of the final day is concerned with using Apache Spark.


### (C) 2017 Darren J Wilkinson


