import java.util.Scanner

import org.apache.spark.sql.SparkSession

object SimpleApp {

  def main(args: Array[String]): Unit = {
    val logFile = "spark-processor/src/main/resources/sampleFile"
    val spark = SparkSession.builder().appName("Simple Application").master("local").getOrCreate()
    val logData = spark.read.textFile(logFile).cache()
    val numsC = logData.filter(line => line.contains("c")).count()
    val numsB = logData.filter(line => line.contains("b")).count()
    val scanner = new Scanner(System.in)
//    scanner.nextLine()
    println(s"Lines with   c: $numsC, Lines with b: $numsB")
    spark.stop()
  }

}
