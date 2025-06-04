package kjstyle.techdom.domain.repository;

import kjstyle.techdom.domain.entitys.DrivingLog;

import java.util.List;

public interface DrivingLogCustomRepository {
    void bulkInsert(List<DrivingLog> drivingLogList);
}