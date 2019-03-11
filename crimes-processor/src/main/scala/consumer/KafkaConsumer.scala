package consumer

import org.apache.kafka.clients.admin.{KafkaAdminClient, NewTopic}
import collection.JavaConverters._
import org.apache.kafka.common.serialization.StringDeserializer
import org.apache.spark.SparkConf
import org.apache.spark.streaming.{Seconds, StreamingContext}
import org.apache.spark.streaming.kafka010._
import org.apache.spark.streaming.kafka010.LocationStrategies.PreferConsistent
import org.apache.spark.streaming.kafka010.ConsumerStrategies.Subscribe

object KafkaConsumer {

  val kafkaParams = Map[String, Object](
    "bootstrap.servers" -> "dev-kafka:9092",
    "key.deserializer" -> classOf[StringDeserializer],
    "value.deserializer" -> classOf[StringDeserializer],
    "group.id" -> "crimes_processor",
    "auto.offset.reset" -> "latest",
    "enable.auto.commit" -> (false: java.lang.Boolean)
  )

  val conf = new SparkConf().setMaster("local[*]")
    .setAppName("SimpleDStreamExample")

  val topic = Array("crimes")

  val streamingContext = new StreamingContext(conf, Seconds(1))


  def run(): Unit = {
    createTopics()
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

  private def createTopics(): Unit = {
    val topic = new NewTopic("crimes", 1, 1)
    val kafkaAdminClient = new KafkaAdminClient()
    kafkaAdminClient.createTopics(List(topic).asJavaCollection)

  }
}
