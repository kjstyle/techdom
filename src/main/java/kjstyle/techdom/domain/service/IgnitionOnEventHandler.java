package kjstyle.techdom.domain.service;

import kjstyle.techdom.domain.exceptions.VehicleEventHandleException;
import kjstyle.techdom.domain.repository.VehicleEventLogRepository;
import kjstyle.techdom.domain.repository.entitys.VehicleEventLog;
import kjstyle.techdom.enums.GpsCondition;
import kjstyle.techdom.enums.VehicleEventType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class IgnitionOnEventHandler implements VehicleEventHandler{

    private final VehicleEventLogRepository vehicleEventLogRepository;

    @Override
    public VehicleEventType getEventType() {
        return VehicleEventType.IGNITION_ON;
    }

    @Override
    public void handle(VehicleEventLog eventLog) {

        // 규격서: "시동 ON 시 GPS 상태가 정상적이지 않으면, GPS 상태 값(gcd)을 ‘P’로 설정하고, 직전 시동 OFF 때의 GPS 위치 정보를 보낸다."
        // 규격서: "위경도 값은 직전 시동 OFF의 위경도 값으로 설정한다."
        if (eventLog.getGpsStatus() == GpsCondition.ABNORMAL_ON_IGNITION) {
            // TODO: DB에서 직전 시동 OFF의 위경도(lat, lon)를 조회하여 eventLog의 lat/lon과 비교하거나 보정하는 로직 추가
            Optional<VehicleEventLog> lastOffEvent = vehicleEventLogRepository.findTopByMdnAndEventTypeOrderByEventTimestampUtcDesc(eventLog.getMdn(), VehicleEventType.IGNITION_OFF);

            lastOffEvent.ifPresentOrElse(
                    prevOffEvent ->{
                        eventLog.adjustGpsPosition(prevOffEvent.getLatitude(), prevOffEvent.getLongitude());
                        log.info("직전 시동 OFF ({})의 위경도({}, {})로 현재 이벤트를 수정. 시동OFF한 곳에서 시동을 다시 걸겠지..",
                                prevOffEvent.getEventTimestampUtc(), prevOffEvent.getLatitude(), prevOffEvent.getLongitude());
                    },
                    () -> {
                        // 규격서: "설치 후, 최초 시동 ON의 경우 그 전에 저장된 GPS 데이터가 없기 때문에 위경도 없이 보낸다. (상태값은 V, GPS 장치 인식 안된 경우는 0)"
                        log.info("이전 OFF가 없다는건..생애최초 시동 걸었다는 이야기인데.. GPS마저 비정상이라면 그냥 0,0 으로 ");
                        eventLog.adjustGpsPosition(0.0, 0.0);
                    }
            );
        }

        vehicleEventLogRepository.save(eventLog);
        log.info("시동 ON 이벤트 저장 완료");
    }
}