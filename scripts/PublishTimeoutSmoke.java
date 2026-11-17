import com.rabbitmq.client.*;
import java.nio.charset.StandardCharsets;

/** 仅用于本地测试：参数为 orderId、appointmentId、scheduleId。 */
class PublishTimeoutSmoke {
    public static void main(String[] args) throws Exception {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        factory.setUsername(System.getenv("RABBITMQ_USERNAME"));
        factory.setPassword(System.getenv("RABBITMQ_PASSWORD"));
        factory.setVirtualHost("hospital");
        try (Connection connection = factory.newConnection(); Channel channel = connection.createChannel()) {
            String json = "{\"orderId\":" + Long.parseLong(args[0]) + ",\"appointmentId\":"
                    + Long.parseLong(args[1]) + ",\"scheduleId\":" + Long.parseLong(args[2]) + ",\"source\":\"timeout\"}";
            channel.confirmSelect();
            for (int i = 0; i < 2; i++) {
                channel.basicPublish("order.cancel.exchange", "order.cancel",
                    new AMQP.BasicProperties.Builder().contentType("application/json").deliveryMode(2).build(),
                    json.getBytes(StandardCharsets.UTF_8));
            }
            channel.waitForConfirmsOrDie(5000);
        }
    }
}
