# Spark intro blog post

## Some rough notes in advance of drafting the post

* http://spark.apache.org/
* https://en.wikipedia.org/wiki/Apache_Spark
* http://spark.apache.org/downloads.html
* http://spark.apache.org/docs/latest/
* http://spark.apache.org/docs/latest/quick-start.html
* http://spark.apache.org/docs/latest/ml-guide.html
* http://spark.apache.org/docs/latest/api/scala/
* http://spark.apache.org/docs/latest/spark-standalone.html


```bash
sudo apt-get update
sudo apt-get install openjdk-8-jdk

wget http://www.eu.apache.org/dist/spark/spark-2.1.0/spark-2.1.0-bin-hadoop2.7.tgz
tar xvfz spark-2.1.0-bin-hadoop2.7.tgz 
cd spark-2.1.0-bin-hadoop2.7
bin/run-example SparkPi 10

sed 's/rootCategory=INFO/rootCategory=WARN/g' < conf/log4j.properties.template > conf/log4j.properties

bin/run-example SparkPi 10

bin/run-example SparkLR

ls examples/src/main/scala/org/apache/spark/examples/

bin/spark-shell --master local[4]
```

Note that while Spark is running, diagnostic information about the "cluster" can be obtained by pointing a web browser port 4040 on the master, which here is just http://localhost:4040/


```scala
sc.textFile("README.md").count


```

* Letter frequencies (and do on /usr/dict/words as well)
* Simulate some log reg data (use mllib to do this?!)
* RDDs and data frames
* Basic descriptive stats
* Filtering rows
* Fitting a simple linear reg model
* Fitting a plain log reg model
* Lasso hyperparam tuning?
* Particle filter? (separate post?)
* Standalone apps with dependencies using sbt...






### eof


