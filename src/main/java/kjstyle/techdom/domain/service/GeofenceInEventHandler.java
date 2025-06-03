package kjstyle.techdom.domain.service;

import kjstyle.techdom.domain.repository.VehicleEventLogRepository;
import kjstyle.techdom.domain.repository.entitys.VehicleEventLog;
import kjstyle.techdom.enums.VehicleEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class GeofenceInEventHandler implements VehicleEventHandler {

    private final VehicleEventLogRepository vehicleEventLogRepository;

    @Override
    public VehicleEventType getEventType() {
        return VehicleEventType.GEOFENCE_IN;
    }

    @Override
    public void handle(VehicleEventLog eventLog) {
        log.info("지오펜스 IN 이벤트 수신 {}", eventLog);

        // TODO : 비지니스 제약사항 추가 필요

        vehicleEventLogRepository.save(eventLog);
        log.info("지오펜스 IN 이벤트 저장 완료 MDN={}", eventLog.getMdn());
    }
}
