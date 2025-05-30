package kjstyle.techdom.domain.repository.entitys;

import jakarta.persistence.*;
import kjstyle.techdom.enums.GpsCondition;
import lombok.*;

import java.math.BigDecimal;

@Entity
@Table(name = "driving_log")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString
public class DrivingLog {

    @EmbeddedId
    private DrivingLogId id;

    @Convert(converter = GpsConditionConverter.class)
    @Column(name = "gps_condition", nullable = false)
    private GpsCondition gpsCondition;

    @Column(precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(precision = 9, scale = 6)
    private BigDecimal longitude;

    private Integer angle;
    private Integer speed;

    @Column(name = "total_distance")
    private Long totalDistance;

    @Column(name = "battery_volt")
    private Integer batteryVolt;
}
