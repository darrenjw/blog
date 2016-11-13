# Spark intro blog post

## Some rough notes in advance of drafting the post


```bash
sudo apt-get update
sudo apt-get install openjdk-8-jdk

wget http://www.eu.apache.org/dist/spark/spark-2.0.2/spark-2.0.2-bin-hadoop2.7.tgz
tar xvfz spark-2.0.0-bin-hadoop2.7.tgz 
cd spark-2.0.0-bin-hadoop2.7
bin/run-example SparkPi 10

sed 's/rootCategory=INFO/rootCategory=WARN/g' < conf/log4j.properties.template > conf/log4j.properties

bin/spark-shell --master local[4]
```

Note that while Spark is running, diagnostic information about the "cluster" can be obtained by pointing a web browser port 4040 on the master, which here is just http://localhost:4040/


```scala
sc.textFile("README.md").count




```









### eof


