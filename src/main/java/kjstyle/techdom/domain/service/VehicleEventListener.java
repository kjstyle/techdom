package kjstyle.techdom.domain.service;

import kjstyle.techdom.config.VehicleKafkaConfig;
import kjstyle.techdom.domain.entitys.VehicleEventLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VehicleEventListener {

    private final VehicleEventLogService vehicleEventLogService;

    @KafkaListener(topics = VehicleKafkaConfig.VEHICLE_EVENT_TOPIC, groupId = "vehicle-event-consumer")
    public void handleEventLogOnKafka(VehicleEventLog eventLog) {
        log.info("--------------- Kafka 메시지 수신 후 핸들러 호출 직전");
        vehicleEventLogService.processVehicleEvent(eventLog);
    }
}
