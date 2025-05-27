package kjstyle.techdom.domain.repository;

import jakarta.persistence.*;
import lombok.*;

import java.time.OffsetDateTime;


@Entity
@Table(name = "device")
@Getter
@NoArgsConstructor
@ToString
@EqualsAndHashCode(of = "id")
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // 단말 고유 ID

    @Column(name = "vehicle_id", unique = true) // UNIQUE 제약 조건만 적용
    private Long vehicleId; // 연결된 차량

    @Column(name = "mdn", unique = true, nullable = false, length = 20)
    private String mdn; // 단말 전화번호 (식별자)

    @Column(name = "terminal_id", nullable = false, length = 10)
    private String terminalId; // 터미널 ID

    @Column(name = "manufacturer_id", nullable = false, length = 10)
    private String manufacturerId; // 제조사 ID

    @Column(name = "packet_version", nullable = false, length = 5)
    private String packetVersion; // 패킷 버전

    @Column(name = "device_type", nullable = false, length = 5)
    private String deviceType; // 장비 유형

    @Column(name = "firmware_version", length = 20)
    private String firmwareVersion; // 펌웨어 버전

    // created_at과 updated_at은 데이터베이스의 DEFAULT NOW()를 따르므로,
    // @Column 어노테이션에 columnDefinition을 명시하여 DDL 생성 시 참고하도록 할 수 있습니다.
    // JPA가 Entity를 영속화할 때 이 필드들을 직접 설정하지 않으면,
    // 데이터베이스의 기본값이 적용됩니다.
    @Column(name = "created_at", columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT NOW()")
    private OffsetDateTime createdAt; // 레코드 생성 일시

    @Column(name = "updated_at", columnDefinition = "TIMESTAMP WITH TIME ZONE DEFAULT NOW()")
    private OffsetDateTime updatedAt; // 레코드 최종 수정 일시
}
