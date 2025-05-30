package kjstyle.techdom.domain.repository;

import kjstyle.techdom.domain.repository.entitys.DrivingLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class DrivingLogCustomRepositoryImpl implements DrivingLogCustomRepository {
    private final JdbcTemplate jdbcTemplate;

    private static final String BULK_INSERT_SQL = """
        INSERT INTO driving_log (
            record_time, mdn, gps_condition, latitude, longitude, angle, speed, total_distance, battery_volt
        ) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
    """;

    @Override
    public void bulkInsert(List<DrivingLog> drivingLogList) {
        int[] updateCounts = jdbcTemplate.batchUpdate(BULK_INSERT_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                var drivingLog = drivingLogList.get(i);
                var id = drivingLog.getId();

                ps.setTimestamp(1, Timestamp.valueOf(id.getRecordTime()));
                ps.setString(2, id.getMdn());
                ps.setString(3, drivingLog.getGpsCondition().getCode());
                ps.setBigDecimal(4, drivingLog.getLatitude());
                ps.setBigDecimal(5, drivingLog.getLongitude());

                if (drivingLog.getAngle() != null) ps.setInt(6, drivingLog.getAngle());
                else ps.setNull(6, Types.INTEGER);

                if (drivingLog.getSpeed() != null) ps.setInt(7, drivingLog.getSpeed());
                else ps.setNull(7, Types.INTEGER);

                if (drivingLog.getTotalDistance() != null) ps.setLong(8, drivingLog.getTotalDistance());
                else ps.setNull(8, Types.BIGINT);

                if (drivingLog.getBatteryVolt() != null) ps.setInt(9, drivingLog.getBatteryVolt());
                else ps.setNull(9, Types.INTEGER);
            }

            @Override
            public int getBatchSize() {
                return drivingLogList.size();
            }
        });
    }
}