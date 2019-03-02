import consumer.KafkaConsumer

object SimpleApp {

  def main(args: Array[String]): Unit = {
    KafkaConsumer.run()
  }
}