/*
spark.scala

code for a "spark-shell" session

/var/tmp/spark-1.4.0-bin-hadoop2.6/bin/spark-shell --packages com.databricks:spark-csv_2.10:1.2.0 --master local[4]

*/

val df = sqlContext.read.format("com.databricks.spark.csv").
                         option("header", "true").
                         option("inferSchema","true").
                         load("../r/cars93.csv")
val df2=df.filter("EngineSize <= 4.0")
val col=df2.col("Weight")*0.453592
val df3=df2.withColumn("WeightKG",col)
df3.write.format("com.databricks.spark.csv").
                         option("header","true").
                         save("out-csv")


// eof


