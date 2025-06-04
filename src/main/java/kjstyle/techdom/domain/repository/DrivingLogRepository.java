package kjstyle.techdom.domain.repository;

import kjstyle.techdom.domain.entitys.DrivingLog;
import kjstyle.techdom.domain.entitys.DrivingLogId;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DrivingLogRepository extends JpaRepository<DrivingLog, DrivingLogId> {
}
