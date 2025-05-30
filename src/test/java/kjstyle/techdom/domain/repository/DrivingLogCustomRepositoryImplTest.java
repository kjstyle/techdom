package kjstyle.techdom.domain.repository;

import kjstyle.techdom.common.BaseTest;
import kjstyle.techdom.domain.repository.entitys.DrivingLog;
import kjstyle.techdom.domain.repository.entitys.DrivingLogId;
import kjstyle.techdom.enums.GpsCondition;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

class DrivingLogCustomRepositoryImplTest extends BaseTest {

    @Autowired
    private DrivingLogCustomRepository drivingLogCustomRepository;

    @Autowired
    private DrivingLogRepository drivingLogRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @Transactional
    void 주행기록_벌크인서트_테스트() {
        // 테스트 기준 시간 설정 (nano초 제거)
        LocalDateTime baseTime = LocalDateTime.now().withNano(0);

        // 10개의 DrivingLog 객체 생성
        List<DrivingLog> logs = IntStream.range(0, 10)
                // IntStream을 각 DrivingLog 객체로 매핑
                .mapToObj(i -> DrivingLog.builder()
                        // ID 값: record_time과 mdn 설정
                        .id(new DrivingLogId(baseTime.plusSeconds(i), "mdn_test_" + i))
                        .gpsCondition(GpsCondition.NORMAL) // GPS 상태 설정
                        .latitude(new BigDecimal("37.0").add(BigDecimal.valueOf(i * 0.001))) // 위도 설정
                        .longitude(new BigDecimal("127.0").add(BigDecimal.valueOf(i * 0.001))) // 경도 설정
                        .angle(10 * i) // 각도 설정
                        .speed(50 + i) // 속도 설정
                        .totalDistance(1000L + i * 100L) // 누적 주행 거리 설정
                        .batteryVolt(120 + i) // 배터리 전압 설정
                        .build())
                .toList();

        // 생성된 DrivingLog 객체들의 ID 출력
        logs.forEach(log -> {
            DrivingLogId id = log.getId();
            System.out.println("record_time: " + id.getRecordTime() + ", mdn: " + id.getMdn());
        });

        // DrivingLog 데이터를 커스텀 레포지토리 메소드로 벌크 삽입
        drivingLogCustomRepository.bulkInsert(logs);

        // DB에서 저장된 결과를 직접 조회하여 출력
        var results = jdbcTemplate.queryForList("SELECT * FROM driving_log");
        results.forEach(System.out::println);

        // 삽입된 데이터에서 첫 번째와 마지막 데이터를 JPA를 통해 조회
        var first = drivingLogRepository.findById(logs.get(0).getId());
        var last = drivingLogRepository.findById(logs.get(logs.size() - 1).getId());

        // 조회된 첫 번째와 마지막 데이터가 존재하는지 확인
        assertThat(first).isPresent();
        assertThat(last).isPresent();

        // 첫 번째 데이터의 위도가 예상값과 같은지 확인
        assertThat(first.get().getLatitude()).isEqualByComparingTo(new BigDecimal("37.0"));
        // 마지막 데이터의 위도가 예상값과 같은지 확인
        assertThat(last.get().getLatitude()).isEqualByComparingTo(new BigDecimal("37.009"));

        // 첫 번째 데이터의 속도가 예상값과 같은지 확인
        assertThat(first.get().getSpeed()).isEqualTo(50);
        // 마지막 데이터의 속도가 예상값과 같은지 확인
        assertThat(last.get().getSpeed()).isEqualTo(59);
    }
}