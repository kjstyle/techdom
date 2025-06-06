package kjstyle.techdom.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import kjstyle.techdom.common.BaseMockMvcTest;
import kjstyle.techdom.domain.service.VehicleEventLogService;
import kjstyle.techdom.enums.VehicleEventType;
import kjstyle.techdom.web.dto.GeofenceEventRequest;
import kjstyle.techdom.web.dto.IgnitionEventRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.ResultActions;
import org.testcontainers.containers.KafkaContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static java.lang.Thread.sleep;
import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Testcontainers
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class VehicleEventControllerTest extends BaseMockMvcTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VehicleEventLogService vehicleEventLogService;


    @Container
    static KafkaContainer kafka = new KafkaContainer(
            DockerImageName.parse("confluentinc/cp-kafka:7.6.1")
    );

    @DynamicPropertySource
    static void configure(DynamicPropertyRegistry registry) {
        kafka.start(); // 컨테이너를 명시적으로 시작
        registry.add("spring.kafka.bootstrap-servers", kafka::getBootstrapServers);
    }

    @Test
    @DisplayName("시동 ON 이벤트 보내고 -> 잘 처리되었는지 확인하기")
    void 시동ON_이벤트_보내고_잘_처리되었는지_확인하기() throws Exception {

        // given
        IgnitionEventRequest req = new IgnitionEventRequest();

        req.setMdn("01012345678");
        req.setTid("A001");
        req.setMid("6");
        req.setPv("5");
        req.setDid("1");

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        req.setOnTime(now);
        req.setOffTime(""); // 시동 ON 이벤트는 offTime이 빈 문자열

        req.setGcd("A");
        req.setLat("37.5665");
        req.setLon("126.9780");
        req.setAng("90");
        req.setSpd("40");
        req.setSum("123456");
        req.setBatteryVolt("128");

        // when
        ResultActions actions = mockMvc.perform(
                post("/api/v1/vehicle/ignition")
                        .contentType(MediaType.APPLICATION_JSON)
                        // .header("Authorization", "Bearer " + testAccessToken) // TODO : 인증 로직 추가 시 주석 풀고 적절히 구현필요
                        .content(objectMapper.writeValueAsString(req))
        );

        // then
        actions.andExpect(status().isOk());


        sleep(1000); // Wait for database synchronization

        vehicleEventLogService.findTopByMdnAndEventTypeOrderByEventTimestampUtcDesc(req.getMdn(), VehicleEventType.IGNITION_ON)
                .ifPresentOrElse(
                    eventLog -> {
                        assertThat(eventLog.getMdn()).isEqualTo(req.getMdn());
                        assertThat(eventLog.getEventType()).isEqualTo(VehicleEventType.IGNITION_ON);
                        assertThat(eventLog.getLatitude()).isEqualTo(37.5665); // 최초이지만 GPS정상 케이스라
                        assertThat(eventLog.getLongitude()).isEqualTo(126.9780); // 최초이지만 GPS정상 케이스라
                        assertThat(eventLog.getBatteryVolt()).isEqualTo(Integer.valueOf(req.getBatteryVolt()));
                        assertThat(eventLog.getSpeed()).isEqualTo(Integer.valueOf(req.getSpd()));
                        assertThat(eventLog.getCurrentAccumulatedDistance()).isEqualTo(Long.valueOf(req.getSum()));
                        assertThat(eventLog.getAngle()).isEqualTo(Integer.valueOf(req.getAng()));
                    },
                    () -> fail("시동 ON 이벤트가 저장되지 않았습니다.")
                );
    }

    @Test
    @DisplayName("400에러 테스트 - MDN이 없어요")
    void _400_에러_테스트_mdn이_없어요 () throws Exception {
        // given
        IgnitionEventRequest req = new IgnitionEventRequest();

        req.setMdn(""); // mdn이 빈물자열로 오면
        req.setTid("A001");
        req.setMid("6");
        req.setPv("5");
        req.setDid("1");

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        req.setOnTime(now);
        req.setOffTime(""); // 시동 ON 이벤트는 offTime이 빈 문자열

        req.setGcd("A");
        req.setLat("37.5665");
        req.setLon("126.9780");
        req.setAng("90");
        req.setSpd("40");
        req.setSum("123456");
        req.setBatteryVolt("128");

        // when
        ResultActions actions = mockMvc.perform(
                post("/api/v1/vehicle/ignition")
                        .contentType(MediaType.APPLICATION_JSON)
                        // .header("Authorization", "Bearer " + testAccessToken) // TODO : 인증 로직 추가 시 주석 풀고 적절히 구현필요
                        .content(objectMapper.writeValueAsString(req))
        );

        // then
        actions.andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("mdn: mdn은 필수 값입니다."))
        ;

    }

    @Test
    @DisplayName("지오펜스 IN 이벤트 보내고 -> 잘 처리되었는지 확인하기")
    void 지오펜스_IN_이벤트_보내고_잘_처리되었는지_확인하기() throws Exception {

        // given
        GeofenceEventRequest req = new GeofenceEventRequest();

        req.setMdn("01012345678");
        req.setTid("A001");
        req.setMid("6");
        req.setPv("5");
        req.setDid("1");

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        req.setGcd("A");
        req.setLat("37.5665");
        req.setLon("126.9780");
        req.setAng("90");
        req.setSpd("40");
        req.setSum("123456");
        req.setBatteryVolt("128");

        req.setGeofenceGroupId("123456");
        req.setGeofencePointId("geo123");

        req.setEventValue("IN");
        req.setEventTime(now);


        // when
        ResultActions actions = mockMvc.perform(
                post("/api/v1/vehicle/geofence/in")
                        .contentType(MediaType.APPLICATION_JSON)
                        // .header("Authorization", "Bearer " + testAccessToken) // TODO : 인증 로직 추가 시 주석 풀고 적절히 구현필요
                        .content(objectMapper.writeValueAsString(req))
        );

        // then
        actions.andExpect(status().isOk());

        sleep(1000); // Wait for database synchronization

        vehicleEventLogService.findTopByMdnAndEventTypeOrderByEventTimestampUtcDesc(req.getMdn(), VehicleEventType.GEOFENCE_IN)
                .ifPresentOrElse(
                        eventLog -> {
                            assertThat(eventLog.getMdn()).isEqualTo(req.getMdn());
                            assertThat(eventLog.getEventType()).isEqualTo(VehicleEventType.GEOFENCE_IN);
                            assertThat(eventLog.getLatitude()).isEqualTo(37.5665);
                            assertThat(eventLog.getLongitude()).isEqualTo(126.9780);
                            assertThat(eventLog.getBatteryVolt()).isEqualTo(Integer.valueOf(req.getBatteryVolt()));
                            assertThat(eventLog.getSpeed()).isEqualTo(Integer.valueOf(req.getSpd()));
                            assertThat(eventLog.getCurrentAccumulatedDistance()).isEqualTo(Long.valueOf(req.getSum()));
                            assertThat(eventLog.getAngle()).isEqualTo(Integer.valueOf(req.getAng()));
                            assertThat(eventLog.getGeofenceGroupId()).isEqualTo(req.getGeofenceGroupId());
                            assertThat(eventLog.getGeofencePointId()).isEqualTo(req.getGeofencePointId());
                        },
                        () -> fail("지오펜스 IN 이벤트가 저장되지 않았습니다.")
                );
    }

    @Test
    @DisplayName("지오펜스 OUT 이벤트 보내고 -> 잘 처리되었는지 확인하기")
    void 지오펜스_OUT_이벤트_보내고_잘_처리되었는지_확인하기() throws Exception {

        // given
        GeofenceEventRequest req = new GeofenceEventRequest();

        req.setMdn("01012345678");
        req.setTid("A001");
        req.setMid("6");
        req.setPv("5");
        req.setDid("1");

        String now = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));

        req.setGcd("A");
        req.setLat("37.5665");
        req.setLon("126.9780");
        req.setAng("90");
        req.setSpd("40");
        req.setSum("123456");
        req.setBatteryVolt("128");

        req.setGeofenceGroupId("123456");
        req.setGeofencePointId("geo123");

        req.setEventValue("OUT");
        req.setEventTime(now);


        // when
        ResultActions actions = mockMvc.perform(
                post("/api/v1/vehicle/geofence/out")
                        .contentType(MediaType.APPLICATION_JSON)
                        // .header("Authorization", "Bearer " + testAccessToken) // TODO : 인증 로직 추가 시 주석 풀고 적절히 구현필요
                        .content(objectMapper.writeValueAsString(req))
        );

        // then
        actions.andExpect(status().isOk());

        sleep(1000); // Wait for database synchronization

        vehicleEventLogService.findTopByMdnAndEventTypeOrderByEventTimestampUtcDesc(req.getMdn(), VehicleEventType.GEOFENCE_OUT)
                .ifPresentOrElse(
                        eventLog -> {
                            assertThat(eventLog.getMdn()).isEqualTo(req.getMdn());
                            assertThat(eventLog.getEventType()).isEqualTo(VehicleEventType.GEOFENCE_OUT);
                            assertThat(eventLog.getLatitude()).isEqualTo(37.5665);
                            assertThat(eventLog.getLongitude()).isEqualTo(126.9780);
                            assertThat(eventLog.getBatteryVolt()).isEqualTo(Integer.valueOf(req.getBatteryVolt()));
                            assertThat(eventLog.getSpeed()).isEqualTo(Integer.valueOf(req.getSpd()));
                            assertThat(eventLog.getCurrentAccumulatedDistance()).isEqualTo(Long.valueOf(req.getSum()));
                            assertThat(eventLog.getAngle()).isEqualTo(Integer.valueOf(req.getAng()));
                            assertThat(eventLog.getGeofenceGroupId()).isEqualTo(req.getGeofenceGroupId());
                            assertThat(eventLog.getGeofencePointId()).isEqualTo(req.getGeofencePointId());
                        },
                        () -> fail("지오펜스 OUT 이벤트가 저장되지 않았습니다.")
                );
    }
}