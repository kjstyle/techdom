package kjstyle.techdom.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import kjstyle.techdom.domain.entitys.VehicleEventLog;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.*;

import org.springframework.kafka.support.serializer.JsonDeserializer;
import org.springframework.kafka.support.serializer.JsonSerializer;

import java.util.HashMap;
import java.util.Map;

@Configuration
public class VehicleKafkaConfig {
    public static final String VEHICLE_EVENT_TOPIC = "vehicle.event.log";


    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers; // ✅ 동적으로 할당된 포트 반영

    @Bean
    public ProducerFactory<String, VehicleEventLog> producerFactory(ObjectMapper objectMapper) {
        Map<String, Object> configProps = new HashMap<>();
//        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092"); // 계속 disconnect 에러
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers); //

        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);
        return new DefaultKafkaProducerFactory<>(configProps, new StringSerializer(), new JsonSerializer<>(objectMapper));
    }

    @Bean
    public KafkaTemplate<String, VehicleEventLog> kafkaTemplate(ProducerFactory<String, VehicleEventLog> pf) {
        return new KafkaTemplate<>(pf);
    }

    @Bean
    public ConsumerFactory<String, VehicleEventLog> consumerFactory(ObjectMapper objectMapper) {
        JsonDeserializer<VehicleEventLog> deserializer = new JsonDeserializer<>(VehicleEventLog.class, objectMapper, false);
        deserializer.addTrustedPackages("*");

        Map<String, Object> props = new HashMap<>();
        props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(ConsumerConfig.GROUP_ID_CONFIG, "vehicle-event-consumer");
        props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest");

        return new DefaultKafkaConsumerFactory<>(props, new StringDeserializer(), deserializer);
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, VehicleEventLog> kafkaListenerContainerFactory(
            ConsumerFactory<String, VehicleEventLog> cf
    ) {
        ConcurrentKafkaListenerContainerFactory<String, VehicleEventLog> factory =
                new ConcurrentKafkaListenerContainerFactory<>();
        factory.setConsumerFactory(cf);
        return factory;
    }
}
