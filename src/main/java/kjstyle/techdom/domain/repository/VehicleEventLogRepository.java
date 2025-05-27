package kjstyle.techdom.domain.repository;

import kjstyle.techdom.domain.repository.entitys.VehicleEventLog;
import kjstyle.techdom.domain.repository.entitys.VehicleEventLogId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VehicleEventLogRepository extends JpaRepository<VehicleEventLog, VehicleEventLogId> {
}