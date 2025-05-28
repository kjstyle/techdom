package kjstyle.techdom.domain.repository.entitys;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

/**
 * 차량 이벤트 로그의 복합 기본키를 정의하는 클래스입니다.
 * vehicle_event_log 테이블의 복합키로 사용되며, 이벤트 발생 시간과 단말기 번호를 조합하여 
 * 고유한 식별자를 생성합니다.
 */
@NoArgsConstructor
@EqualsAndHashCode
public class VehicleEventLogId implements Serializable {
    /**
     * 차량 이벤트가 발생한 UTC 기준 시각
     */
    private OffsetDateTime eventTimestampUtc;
    /**
     * 차량 단말기 식별 번호 (Mobile Directory Number)
     */
    private String mdn;
}
