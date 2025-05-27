package kjstyle.techdom.domain.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import kjstyle.techdom.common.BaseTest;
import kjstyle.techdom.domain.repository.VehicleEventLogRepository;
import kjstyle.techdom.domain.repository.entitys.VehicleEventLog;
import kjstyle.techdom.enums.GpsCondition;
import kjstyle.techdom.enums.VehicleEventType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class VehicleEventLogServiceTest extends BaseTest {

    @Autowired
    private VehicleEventLogService vehicleEventLogService;


    private final ObjectMapper objectMapper;

    // **생성자에서 ObjectMapper를 초기화하고 JavaTimeModule을 등록합니다.**
    public VehicleEventLogServiceTest() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule()); // **이 라인이 핵심입니다.**
    }

    @Test
    @DisplayName("주행이벤트_저장해보기")
    void 주행이벤트_저장해보기() {

        // 1. 시동ON에 해당하는 VehicleEventLog 객체를 생성한다.
        // 2. 로우데이터 항목은 자신을 json 문자열로 변경한 문자열을 set하도록한다.
        // 3. saveEventLog를 호출하고
        // 4. 리턴된 객체와 1에서 생성한 객체의 2가지 항목 값이 일치하면 성공으로 판단

        VehicleEventLog eventLogToSave = new VehicleEventLog();
        OffsetDateTime now = OffsetDateTime.now(ZoneOffset.UTC); // 현재 UTC 시간
        String mdn = "01099998888"; // 테스트용 MDN

        eventLogToSave.setEventTimestampUtc(now);
        eventLogToSave.setMdn(mdn);
        eventLogToSave.setEventType(VehicleEventType.IGNITION_ON); // 시동 ON 이벤트
        eventLogToSave.setEventSecond(now.getSecond());
        eventLogToSave.setGpsStatus(GpsCondition.NORMAL);
        eventLogToSave.setLatitude(37.123456);
        eventLogToSave.setLongitude(127.654321);
        eventLogToSave.setAngle(180);
        eventLogToSave.setSpeed(0); // 시동 ON 시 속도는 0
        eventLogToSave.setCurrentAccumulatedDistance(100000L);
        eventLogToSave.setBatteryVolt(120);
        eventLogToSave.setOnTime(now); // 시동 ON 시간

        try {
            // ObjectMapper는 기본적으로 getter 메서드를 사용하여 객체를 JSON으로 변환합니다.
            // transient 필드나 @JsonIgnore 어노테이션이 없는 한 모든 필드가 포함될 수 있습니다.
            String rawJsonString = objectMapper.writeValueAsString(eventLogToSave);
            eventLogToSave.setRawJsonData(rawJsonString);
        } catch (Exception e) {
            // JSON 변환 중 오류 발생 시 테스트 실패 처리
            throw new RuntimeException("Failed to serialize VehicleEventLog object to JSON string for rawData", e);
        }

        // 3. saveEventLog를 호출하고
        VehicleEventLog savedEventLog = vehicleEventLogService.saveEventLog(eventLogToSave);

        // 4. 리턴된 객체와 1에서 생성한 객체의 2가지 항목 값이 일치하면 성공으로 판단
        // 복합 키인 eventTimestampUtc와 mdn이 잘 저장되었는지 확인합니다.
        assertThat(savedEventLog).isNotNull();
        assertThat(savedEventLog.getEventTimestampUtc()).isEqualTo(eventLogToSave.getEventTimestampUtc());
        assertThat(savedEventLog.getMdn()).isEqualTo(eventLogToSave.getMdn());
    }
}