package kjstyle.techdom.domain.service;

import kjstyle.techdom.domain.repository.DrivingLogCustomRepository;
import kjstyle.techdom.domain.repository.DrivingLogRepository;
import kjstyle.techdom.domain.repository.entitys.DrivingLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DrivingLogService {

    private final DrivingLogRepository drivingLogRepository;
    private final DrivingLogCustomRepository drivingLogCustomRepository;

    public DrivingLog saveDrivingLog(DrivingLog drivingLog) {
        return drivingLogRepository.save(drivingLog);
    }

    public void saveBulk(List<DrivingLog> logs) {
        log.info("Saving bulk logs:\n" + logs.stream()
                .map(Object::toString)
                .collect(Collectors.joining("\n")));
        drivingLogCustomRepository.bulkInsert(logs);
    }
}
