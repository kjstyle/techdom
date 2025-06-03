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
public class IgnitionOffEventHandler implements VehicleEventHandler{

    private final VehicleEventLogRepository vehicleEventLogRepository;

    @Override
    public VehicleEventType getEventType() {
        return VehicleEventType.IGNITION_OFF;
    }

    @Override
    public void handle(VehicleEventLog eventLog) {

        // TODO : 비지니스 제약사항 추가 필요

        vehicleEventLogRepository.save(eventLog);

        // TODO : 차량의 상태를 운행종료로 업데이트해야함
        // TODO : 차량의 최종 누적거리를 업데이트해야함

        log.info("시동 OFF 이벤트 저장 완료 MDN={}", eventLog.getMdn());
    }
}