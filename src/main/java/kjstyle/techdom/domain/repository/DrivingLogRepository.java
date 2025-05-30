package kjstyle.techdom.domain.repository;

import kjstyle.techdom.domain.repository.entitys.DrivingLog;
import kjstyle.techdom.domain.repository.entitys.DrivingLogId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DrivingLogRepository extends JpaRepository<DrivingLog, DrivingLogId> {
}
