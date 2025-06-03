package kjstyle.techdom.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import kjstyle.techdom.common.BaseMockMvcTest;
import kjstyle.techdom.domain.service.VehicleEventLogService;
import kjstyle.techdom.enums.VehicleEventType;
import kjstyle.techdom.web.dto.IgnitionEventRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.fail;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static java.lang.Thread.sleep;

class IgnitionEventControllerTest extends BaseMockMvcTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VehicleEventLogService vehicleEventLogService;

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
                        assertThat(eventLog.getLatitude()).isEqualTo(0.0); // 이전 기록이 없을거라 0.0
                        assertThat(eventLog.getLongitude()).isEqualTo(0.0); // 이전 기록이 없을거라 0.0
                        assertThat(eventLog.getBatteryVolt()).isEqualTo(Integer.valueOf(req.getBatteryVolt()));
                        assertThat(eventLog.getSpeed()).isEqualTo(Integer.valueOf(req.getSpd()));
                        assertThat(eventLog.getCurrentAccumulatedDistance()).isEqualTo(Long.valueOf(req.getSum()));
                        assertThat(eventLog.getAngle()).isEqualTo(Integer.valueOf(req.getAng()));
                    },
                    () -> fail("시동 ON 이벤트가 저장되지 않았습니다.")
                );
    }
}