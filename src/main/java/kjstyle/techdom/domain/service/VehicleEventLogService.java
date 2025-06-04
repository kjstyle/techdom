package kjstyle.techdom.domain.service;

import kjstyle.techdom.domain.entitys.VehicleEventLog;
import kjstyle.techdom.domain.exceptions.VehicleEventHandleException;
import kjstyle.techdom.domain.repository.VehicleEventLogRepository;
import kjstyle.techdom.enums.VehicleEventType;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;


/**
 * 차량 추적 시스템 내의 차량 이벤트 로그를 관리하는 서비스 클래스입니다.
 * 위치 업데이트, 시동 상태 변경 및 기타 운영 이벤트와 같은
 * 차량 관련 이벤트의 저장 및 조회를 처리합니다.
 */
@Slf4j
@Service
public class VehicleEventLogService {
    private final VehicleEventLogRepository vehicleEventLogRepository;

    private final Map<VehicleEventType, VehicleEventHandler> eventHandlers;

    @Autowired
    public VehicleEventLogService(
            VehicleEventLogRepository vehicleEventLogRepository,
            List<VehicleEventHandler> handlers // Spring이 모든 VehicleEventHandler 구현체를 리스트로 주입
    ) {
        this.vehicleEventLogRepository = vehicleEventLogRepository;

        // 이벤트 타입에 대한 VehicleEventHandler 객체를 Map으로 저장
        this.eventHandlers = handlers.stream()
                .collect(Collectors.toMap(VehicleEventHandler::getEventType, Function.identity()));
    }

    public VehicleEventLog saveEventLog(VehicleEventLog vehicleEventLog) {
        return vehicleEventLogRepository.save(vehicleEventLog);
    }

    public void processVehicleEvent(VehicleEventLog eventLog) throws VehicleEventHandleException {
        log.info("MDN: {} 이벤트 수신 {}", eventLog.getMdn(), eventLog);

        VehicleEventHandler handler = eventHandlers.get(eventLog.getEventType());
        if (handler != null) {
            handler.handle(eventLog);
        } else {
            log.error("지원하지 않는 이벤트 타입입니다.");
            throw new VehicleEventHandleException("지원하지 않는 이벤트 타입입니다.");
        }
    }

    public Optional<VehicleEventLog> findTopByMdnAndEventTypeOrderByEventTimestampUtcDesc(String mdn, VehicleEventType eventType) {
        return vehicleEventLogRepository.findTopByMdnAndEventTypeOrderByEventTimestampUtcDesc(mdn, eventType);
    }
}