package kjstyle.techdom.domain.service;

import kjstyle.techdom.config.VehicleKafkaConfig;
import kjstyle.techdom.domain.entitys.VehicleEventLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class EventSender {

    private final KafkaTemplate<String, VehicleEventLog> kafkaTemplate;

    public void send(VehicleEventLog eventLog) {
        log.info("--------------- 이벤트 퍼블리싱 직전");
        String kafkaKey = generateKafkaKey(eventLog.getMdn(), eventLog.getEventTimestampUtc());
        kafkaTemplate.send(VehicleKafkaConfig.VEHICLE_EVENT_TOPIC, kafkaKey, eventLog);
        log.info("--------------- 이벤트 퍼블리싱 직후");
    }

    private static String generateKafkaKey(String mdn, OffsetDateTime eventTimestampUtc) {
        return mdn + "-" + eventTimestampUtc;
    }
}
