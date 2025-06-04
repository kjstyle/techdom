package kjstyle.techdom.domain.repository;

import kjstyle.techdom.domain.entitys.VehicleEventLog;
import kjstyle.techdom.domain.entitys.VehicleEventLogId;
import kjstyle.techdom.enums.VehicleEventType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VehicleEventLogRepository extends JpaRepository<VehicleEventLog, VehicleEventLogId> {

    Optional<VehicleEventLog> findTopByMdnAndEventTypeOrderByEventTimestampUtcDesc(String mdn, VehicleEventType eventType);

    long countByMdnAndEventType(String mdn, VehicleEventType eventType);
}