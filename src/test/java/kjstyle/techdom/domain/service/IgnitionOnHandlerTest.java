package kjstyle.techdom.domain.service;

import kjstyle.techdom.domain.exceptions.VehicleEventHandleException;
import kjstyle.techdom.domain.repository.VehicleEventLogRepository;
import kjstyle.techdom.domain.repository.entitys.VehicleEventLog;
import kjstyle.techdom.enums.GpsCondition;
import kjstyle.techdom.enums.VehicleEventType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("IgnitionOnEventHandler 테스트")
class IgnitionOnEventHandlerTest {

    @Mock
    private VehicleEventLogRepository vehicleEventLogRepository;

    @InjectMocks
    private IgnitionOnEventHandler ignitionOnEventHandler;

    private VehicleEventLog baseEventLog;

    @BeforeEach
    void setUp() {
        // 기본 시동 ON 이벤트 로그 설정
        baseEventLog = VehicleEventLog.builder()
                .eventTimestampUtc(OffsetDateTime.of(2023, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC))
                .mdn("01012345678")
                .eventType(VehicleEventType.IGNITION_ON)
                .gpsStatus(GpsCondition.NORMAL)
                .latitude(37.5665)
                .longitude(126.9780)
                .angle(0)
                .speed(0)
                .currentAccumulatedDistance(100L)
                .onTime(OffsetDateTime.of(2023, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC))
                .rawJsonData("{}")
                .build();
    }

    @Test
    @DisplayName("일반적인 시동 ON 이벤트 처리")
    void handle_NormalIgnitionOnEvent_ShouldProcessSuccessfully() {
        // given
        when(vehicleEventLogRepository.countByMdnAndEventType(anyString(), eq(VehicleEventType.IGNITION_ON)))
                .thenReturn(1L); // 첫 시동 ON이 아님

        // when
        assertDoesNotThrow(() -> ignitionOnEventHandler.handle(baseEventLog));

        // then
        // 특정 로직에 대한 Mock 호출 검증 (예: GPS 상태 'P'가 아니므로 findTopBy...는 호출되지 않음)
        verify(vehicleEventLogRepository, never()).findTopByMdnAndEventTypeOrderByEventTimestampUtcDesc(anyString(), any());
        verify(vehicleEventLogRepository, atLeastOnce()).countByMdnAndEventType(anyString(), eq(VehicleEventType.IGNITION_ON));
    }

    @Test
    @DisplayName("GPS 상태 'P'인 시동 ON 이벤트 처리 - 직전 OFF 이벤트 존재")
    void handle_IgnitionOnWithGpsP_ShouldLogWarningAndReferPreviousOffEvent() {
        // given
        baseEventLog = VehicleEventLog.builder()
                .eventTimestampUtc(OffsetDateTime.of(2023, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC))
                .mdn("01012345678")
                .eventType(VehicleEventType.IGNITION_ON)
                .gpsStatus(GpsCondition.ABNORMAL_ON_IGNITION)
                .latitude(37.5665)
                .longitude(126.9780)
                .angle(0)
                .speed(0)
                .currentAccumulatedDistance(100L)
                .onTime(OffsetDateTime.of(2023, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC))
                .rawJsonData("{}")
                .build();

        VehicleEventLog prevOffEvent = VehicleEventLog.builder()
                .eventTimestampUtc(OffsetDateTime.of(2023, 1, 1, 9, 50, 0, 0, ZoneOffset.UTC))
                .mdn("01012345678")
                .eventType(VehicleEventType.IGNITION_OFF)
                .latitude(37.5600)
                .longitude(126.9700)
                .build();

        when(vehicleEventLogRepository.countByMdnAndEventType(anyString(), eq(VehicleEventType.IGNITION_ON)))
                .thenReturn(1L);
        when(vehicleEventLogRepository.findTopByMdnAndEventTypeOrderByEventTimestampUtcDesc(eq(baseEventLog.getMdn()), eq(VehicleEventType.IGNITION_OFF)))
                .thenReturn(Optional.of(prevOffEvent));

        // when
        assertDoesNotThrow(() -> ignitionOnEventHandler.handle(baseEventLog));

        // then
        verify(vehicleEventLogRepository, atLeastOnce()).findTopByMdnAndEventTypeOrderByEventTimestampUtcDesc(eq(baseEventLog.getMdn()), eq(VehicleEventType.IGNITION_OFF));
    }

    @Test
    @DisplayName("GPS 상태 'P'인 시동 ON 이벤트 처리 - 직전 OFF 이벤트 없음")
    void handle_IgnitionOnWithGpsP_ShouldLogWarningWhenNoPreviousOffEvent() {
        // given
        baseEventLog = VehicleEventLog.builder()
                .eventTimestampUtc(OffsetDateTime.of(2023, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC))
                .mdn("01012345678")
                .eventType(VehicleEventType.IGNITION_ON)
                .gpsStatus(GpsCondition.ABNORMAL_ON_IGNITION)
                .latitude(37.5665)
                .longitude(126.9780)
                .angle(0)
                .speed(0)
                .currentAccumulatedDistance(100L)
                .onTime(OffsetDateTime.of(2023, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC))
                .rawJsonData("{}")
                .build();

        when(vehicleEventLogRepository.countByMdnAndEventType(anyString(), eq(VehicleEventType.IGNITION_ON)))
                .thenReturn(1L);
        when(vehicleEventLogRepository.findTopByMdnAndEventTypeOrderByEventTimestampUtcDesc(eq(baseEventLog.getMdn()), eq(VehicleEventType.IGNITION_OFF)))
                .thenReturn(Optional.empty());

        // when
        assertDoesNotThrow(() -> ignitionOnEventHandler.handle(baseEventLog));

        // then
        verify(vehicleEventLogRepository, atLeastOnce()).findTopByMdnAndEventTypeOrderByEventTimestampUtcDesc(eq(baseEventLog.getMdn()), eq(VehicleEventType.IGNITION_OFF));
    }

    @Test
    @DisplayName("최초 시동 ON 이벤트 처리 - 규격 준수 (GPS V, 0, lat/lon 0.0)")
    void handle_FirstIgnitionOnEvent_ShouldProcessSuccessfullyWithValidGpsV0() {
        // given
        baseEventLog = VehicleEventLog.builder()
                .eventTimestampUtc(OffsetDateTime.of(2023, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC))
                .mdn("01012345678")
                .eventType(VehicleEventType.IGNITION_ON)
                .gpsStatus(GpsCondition.ABNORMAL)
                .latitude(0.0)
                .longitude(0.0)
                .angle(0)
                .speed(0)
                .currentAccumulatedDistance(100L)
                .onTime(OffsetDateTime.of(2023, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC))
                .rawJsonData("{}")
                .build();

        when(vehicleEventLogRepository.countByMdnAndEventType(anyString(), eq(VehicleEventType.IGNITION_ON)))
                .thenReturn(0L); // 최초 시동 ON

        // when
        assertDoesNotThrow(() -> ignitionOnEventHandler.handle(baseEventLog));

        // then
        verify(vehicleEventLogRepository, atLeastOnce()).countByMdnAndEventType(anyString(), eq(VehicleEventType.IGNITION_ON));
    }

    @Test
    @DisplayName("최초 시동 ON 이벤트 처리 - 규격 불일치 (GPS A, lat/lon 0.0)")
    void handle_FirstIgnitionOnEvent_ShouldThrowExceptionWithInvalidGpsA() {
        // given
        baseEventLog = VehicleEventLog.builder()
                .eventTimestampUtc(OffsetDateTime.of(2023, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC))
                .mdn("01012345678")
                .eventType(VehicleEventType.IGNITION_ON)
                .gpsStatus(GpsCondition.NORMAL)
                .latitude(0.0)
                .longitude(0.0)
                .angle(0)
                .speed(0)
                .currentAccumulatedDistance(100L)
                .onTime(OffsetDateTime.of(2023, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC))
                .rawJsonData("{}")
                .build();
        when(vehicleEventLogRepository.countByMdnAndEventType(anyString(), eq(VehicleEventType.IGNITION_ON)))
                .thenReturn(0L); // 최초 시동 ON

        // when
        VehicleEventHandleException exception = assertThrows(VehicleEventHandleException.class, () ->
                ignitionOnEventHandler.handle(baseEventLog)
        );

        // then
        assertEquals("최초 시동 ON 규격 불일치", exception.getMessage());
        assertEquals("400", exception.getErrorCode());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        verify(vehicleEventLogRepository, atLeastOnce()).countByMdnAndEventType(anyString(), eq(VehicleEventType.IGNITION_ON));
    }

    @Test
    @DisplayName("최초 시동 ON 이벤트 처리 - 규격 불일치 (GPS V, lat/lon 정상)")
    void handle_FirstIgnitionOnEvent_ShouldThrowExceptionWithValidGpsAndInvalidLatLon() {
        // given
        baseEventLog = VehicleEventLog.builder()
                .eventTimestampUtc(OffsetDateTime.of(2023, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC))
                .mdn("01012345678")
                .eventType(VehicleEventType.IGNITION_ON)
                .gpsStatus(GpsCondition.ABNORMAL)
                .latitude(37.5665)
                .longitude(126.9780)
                .angle(0)
                .speed(0)
                .currentAccumulatedDistance(100L)
                .onTime(OffsetDateTime.of(2023, 1, 1, 10, 0, 0, 0, ZoneOffset.UTC))
                .rawJsonData("{}")
                .build();
        when(vehicleEventLogRepository.countByMdnAndEventType(anyString(), eq(VehicleEventType.IGNITION_ON)))
                .thenReturn(0L); // 최초 시동 ON

        // when
        VehicleEventHandleException exception = assertThrows(VehicleEventHandleException.class, () ->
                ignitionOnEventHandler.handle(baseEventLog)
        );

        // then
        assertEquals("최초 시동 ON 규격 불일치", exception.getMessage());
        assertEquals("400", exception.getErrorCode());
        assertEquals(HttpStatus.BAD_REQUEST, exception.getHttpStatus());
        verify(vehicleEventLogRepository, atLeastOnce()).countByMdnAndEventType(anyString(), eq(VehicleEventType.IGNITION_ON));
    }
}