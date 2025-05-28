package kjstyle.techdom.domain.service;

import kjstyle.techdom.domain.repository.VehicleEventLogRepository;
import kjstyle.techdom.domain.repository.entitys.VehicleEventLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


/**
 * 차량 추적 시스템 내의 차량 이벤트 로그를 관리하는 서비스 클래스입니다.
 * 위치 업데이트, 시동 상태 변경 및 기타 운영 이벤트와 같은
 * 차량 관련 이벤트의 저장 및 조회를 처리합니다.
 */
@Service
@RequiredArgsConstructor
public class VehicleEventLogService {
    private final VehicleEventLogRepository vehicleEventLogRepository;

    /**
     * 차량 이벤트 로그 항목을 영속성 저장소에 저장합니다.
     *
     * @param vehicleEventLog 저장할 차량 이벤트 로그 엔티티
     * @return 생성된 ID 또는 업데이트된 필드가 포함된 저장된 차량 이벤트 로그 엔티티
     */
    public VehicleEventLog saveEventLog(VehicleEventLog vehicleEventLog) {
        return vehicleEventLogRepository.save(vehicleEventLog);
    }
}