package consumer

import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

object KafkaConsumer {

  val kafkaParams = Map[String, Object](
    "bootstrap.servers" -> "172.17.0.13:9092",
    "key.deserializer" -> classOf[StringDeserializer],
    "value.deserializer" -> classOf[StringDeserializer],
    "group.id" -> "use_a_separate_group_id_for_each_stream",
    "auto.offset.reset" -> "latest",
    "enable.auto.commit" -> (false: java.lang.Boolean)
  )

  val conf = new SparkConf().setMaster("local[*]")
    .setAppName("SimpleDStreamExample")

  val topic = Array("admintome-test")

  val streamingContext = new StreamingContext(conf, Seconds(1))


  def run(): Unit = {
    val stream = KafkaUtils.createDirectStream(
      streamingContext,
      PreferConsistent,
      Subscribe[String, String](topic, kafkaParams)
    )
    stream.map(record => {
      println(record.key())
      println(record.value())
    })
  }
}
