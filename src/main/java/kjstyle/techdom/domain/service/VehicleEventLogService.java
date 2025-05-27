package kjstyle.techdom.domain.service;

import kjstyle.techdom.domain.repository.VehicleEventLogRepository;
import kjstyle.techdom.domain.repository.entitys.VehicleEventLog;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class VehicleEventLogService {
    private final VehicleEventLogRepository vehicleEventLogRepository;

    public VehicleEventLog saveEventLog(VehicleEventLog vehicleEventLog) {
        return vehicleEventLogRepository.save(vehicleEventLog);
    }
}