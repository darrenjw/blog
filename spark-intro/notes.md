# Spark intro blog post

## Some rough notes in advance of drafting the post


```bash

sudo apt-get install openjdk-8-jdk

wget http://www.eu.apache.org/dist//spark/spark-2.0.0/spark-2.0.0-bin-hadoop2.7.tgz
tar xvfz spark-2.0.0-bin-hadoop2.7.tgz 
cd spark-2.0.0-bin-hadoop2.7
bin/run-example SparkPi 10

sed 's/rootCategory=INFO/rootCategory=WARN/g' < conf/log4j.properties.template > conf/log4j.properties

bin/spark-shell --master local[4]
sc.textFile("README.md").count

http://localhost:4040/






```









### eof


