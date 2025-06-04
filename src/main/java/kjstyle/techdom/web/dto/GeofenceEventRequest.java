package kjstyle.techdom.web.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import kjstyle.techdom.domain.entitys.VehicleEventLog;
import kjstyle.techdom.enums.GpsCondition;
import kjstyle.techdom.enums.VehicleEventType;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

@Getter
@Setter
@ToString
public class GeofenceEventRequest {

    // 공통 필수 필드
    @NotBlank(message = "mdn은 필수 값입니다.")
    private String mdn;

    @NotBlank(message = "tid는 필수 값입니다.")
    @Pattern(regexp = "^A001$", message = "tid는 'A001'로 고정되어야 합니다.")
    private String tid;

    @NotBlank(message = "mid는 필수 값입니다.")
    @Pattern(regexp = "^6$", message = "mid는 '6' 값이어야 합니다.")
    private String mid;

    @NotBlank(message = "pv는 필수 값입니다.")
    @Pattern(regexp = "^5$", message = "pv는 '5' 값이어야 합니다.")
    private String pv;

    @NotBlank(message = "did는 필수 값입니다.")
    @Pattern(regexp = "^1$", message = "did는 '1' 값이어야 합니다.")
    private String did;

    // 이벤트 시간 필드
    @NotBlank(message = "eventTime은 필수 값입니다.")
    @Pattern(regexp = "\\d{14}", message = "eventTime은 'ccyyMMddHHmmss' 형식의 14자리 숫자여야 합니다.")
    private String eventTime; // 지오펜싱 이벤트 발생 시각

    // GPS 관련 필드
    @NotBlank(message = "gcd는 필수 값입니다.")
    @Pattern(regexp = "^(A|V|0|P)$", message = "gcd는 'A', 'V', '0', 'P' 중 하나여야 합니다.")
    private String gcd; // GPS 상태 (GpsCondition Enum의 String 값)

    @NotBlank(message = "lat은 필수 값입니다.")
    @Pattern(regexp = "-?\\d+(\\.\\d+)?", message = "lat은 유효한 숫자 형식이어야 합니다.")
    private String lat;

    @NotBlank(message = "lon은 필수 값입니다.")
    @Pattern(regexp = "-?\\d+(\\.\\d+)?", message = "lon은 유효한 숫자 형식이어야 합니다.")
    private String lon;

    @NotBlank(message = "ang은 필수 값입니다.")
    @Pattern(regexp = "\\d+", message = "ang은 숫자 형식이어야 합니다.")
    private String ang;

    @NotBlank(message = "spd는 필수 값입니다.")
    @Pattern(regexp = "\\d+", message = "spd는 숫자 형식이어야 합니다.")
    private String spd;

    @NotBlank(message = "sum은 필수 값입니다.")
    @Pattern(regexp = "\\d+", message = "sum은 숫자 형식이어야 합니다.")
    private String sum; // 누적 주행 거리

    // 배터리 관련 필드 (옵션)
    @Pattern(regexp = "\\d+", message = "batteryVolt는 숫자 형식이어야 합니다.")
    private String batteryVolt; // 배터리 전압

    // 지오펜싱 전용 필드
    @NotBlank(message = "geofenceGroupId는 필수 값입니다.")
    private String geofenceGroupId;

    @NotBlank(message = "geofencePointId는 필수 값입니다.")
    private String geofencePointId;

    @NotBlank(message = "eventValue는 필수 값입니다.")
    // 지오펜싱 IN/OUT을 명확히 구분하는 값 (예: "IN", "OUT")
    @Pattern(regexp = "^(IN|OUT)$", message = "eventValue는 'IN' 또는 'OUT'이어야 합니다.")
    private String eventValue;

    // 'ccyyMMddHHmmss' 형식의 날짜/시간 파싱을 위한 포맷터
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

    /**
     * GeofenceEventRequest DTO를 VehicleEventLog 엔티티로 변환하는 메서드.
     * 지오펜싱 IN/OUT 이벤트에 특화된 매핑을 수행합니다.
     *
     * @param rawJsonData 원본 JSON 문자열 (로그 저장용)
     * @param eventType   컨트롤러에서 명시적으로 전달하는 이벤트 타입 (GEOFENCE_IN 또는 GEOFENCE_OUT)
     * @return VehicleEventLog 엔티티
     * @throws IllegalArgumentException 파싱 오류 발생 시
     */
    public VehicleEventLog toVehicleEventLog(String rawJsonData, VehicleEventType eventType) {
        try {
            // 날짜/시간 파싱
            LocalDateTime parsedEventTime = LocalDateTime.parse(this.eventTime, FORMATTER);

            // 숫자 필드 파싱
            Double parsedLat = Double.parseDouble(this.lat);
            Double parsedLon = Double.parseDouble(this.lon);
            Integer parsedAng = Integer.parseInt(this.ang);
            Integer parsedSpd = Integer.parseInt(this.spd);
            Long parsedSum = Long.parseLong(this.sum);
            Integer parsedBatteryVolt = (this.batteryVolt != null && !this.batteryVolt.isEmpty()) ?
                    Integer.parseInt(this.batteryVolt) : null;

            // VehicleEventLog.Builder를 사용하여 객체 생성 (setter 사용 최소화)
            return VehicleEventLog.builder()
                    .eventTimestampUtc(parsedEventTime.atOffset(ZoneOffset.UTC))
                    .mdn(this.mdn)
                    .eventType(eventType) // 컨트롤러에서 전달받은 이벤트 타입 설정
                    .gpsStatus(GpsCondition.fromCode(this.gcd))
                    .latitude(parsedLat)
                    .longitude(parsedLon)
                    .angle(parsedAng)
                    .speed(parsedSpd)
                    .currentAccumulatedDistance(parsedSum)
                    .batteryVolt(parsedBatteryVolt) // null 허용
                    .geofenceGroupId(this.geofenceGroupId)
                    .geofencePointId(this.geofencePointId)
                    .eventValue(this.eventValue)
                    // 시동 관련 필드는 지오펜싱 이벤트에서는 사용하지 않으므로 빌더에서 설정하지 않음 (기본값 null)
                    .onTime(null)
                    .ignitionOffTime(null)
                    .rawJsonData(rawJsonData)
                    .build();

        } catch (DateTimeParseException e) {
            throw new IllegalArgumentException("날짜/시간 형식 오류: " + e.getMessage(), e);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("숫자 형식 오류: " + e.getMessage(), e);
        }
    }
}
