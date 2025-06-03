package kjstyle.techdom.domain.repository.entitys;

import kjstyle.techdom.enums.GpsCondition;
import kjstyle.techdom.enums.VehicleEventType;
import lombok.*;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;

@Entity
@Table(name = "vehicle_event_log")
@IdClass(VehicleEventLogId.class)
@NoArgsConstructor
@Getter
@ToString
@AllArgsConstructor
@Builder
public class VehicleEventLog {

    @Id
    @Column(name = "event_timestamp_utc", nullable = false)
    private OffsetDateTime eventTimestampUtc;

    @Id
    @Column(name = "mdn", nullable = false, length = 20)
    private String mdn;

    @jakarta.persistence.Enumerated(jakarta.persistence.EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private VehicleEventType eventType;

    @Column(name = "event_second")
    private Integer eventSecond;

    @Column(name = "gps_status") // GpsCondition Converter가 자동 적용됩니다.
    private GpsCondition gpsStatus; // GPS 상태 (ENUM 사용)

    @Column(name = "latitude", precision = 9)
    private Double latitude;

    @Column(name = "longitude", precision = 9)
    private Double longitude;

    @Column(name = "angle")
    private Integer angle;

    @Column(name = "speed")
    private Integer speed;

    @Column(name = "current_accumulated_distance")
    private Long currentAccumulatedDistance; // Total distance traveled by vehicle in meters

    @Column(name = "battery_volt")
    private Integer batteryVolt;

    @Column(name = "on_time")
    private OffsetDateTime onTime;

    /**
     * 차량의 시동이 꺼진 시각(시동 OFF 이벤트 발생 시각)
     * - UTC 기준 시각으로 저장됨
     * - 운행 종료 시점 기록용으로 사용
     * - {@link VehicleEventType#IGNITION_OFF} 이벤트와 연관됨
     */
    @Column(name = "ignition_off_time") // 시동 꺼짐 이벤트 발생 시각 (UTC)
    private OffsetDateTime ignitionOffTime;

    @Column(name = "geofence_group_id", length = 20)
    private String geofenceGroupId;

    @Column(name = "geofence_point_id", length = 20)
    private String geofencePointId;

    @Column(name = "event_value", length = 10)
    private String eventValue;

    @Column(name = "raw_json_data", columnDefinition = "JSONB")
    private String rawJsonData;

    public void adjustGpsPosition(Double newLat, Double newLon) {
        this.latitude = newLat;
        this.longitude = newLon;
    }
}