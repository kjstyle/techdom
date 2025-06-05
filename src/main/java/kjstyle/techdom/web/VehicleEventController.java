package kjstyle.techdom.web;

import jakarta.validation.Valid;
import kjstyle.techdom.config.VehicleKafkaConfig;
import kjstyle.techdom.domain.entitys.VehicleEventLog;
import kjstyle.techdom.domain.service.EventSender;
import kjstyle.techdom.enums.VehicleEventType;
import kjstyle.techdom.web.dto.EventResponse;
import kjstyle.techdom.web.dto.GeofenceEventRequest;
import kjstyle.techdom.web.dto.IgnitionEventRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

@Slf4j
@RestController
@RequiredArgsConstructor
public class VehicleEventController {

    private final EventSender eventSender;

    @PostMapping("/api/v1/vehicle/ignition")
    public ResponseEntity<EventResponse> ignition(@Valid @RequestBody IgnitionEventRequest request) { // TODO : 응답을 어떻게 해야할지
        log.info("시동 이벤트 컨트롤러 진입 : {} ", request);

        log.info("--------------- 이벤트 퍼블리싱 직전");
        eventSender.send(request.toVehicleEventLog());
        log.info("--------------- 이벤트 퍼블리싱 직후");

        return new ResponseEntity<>(
                new EventResponse("200", "OK", request.getMdn())
                , HttpStatus.OK
        );
    }

    @PostMapping("/api/v1/vehicle/geofence/in")
    public ResponseEntity<EventResponse> geofenceIn(@Valid @RequestBody GeofenceEventRequest request) {
        log.info("지오펜스 IN 컨트롤러 진입 : {} ", request);
        log.info("--------------- 이벤트 퍼블리싱 직전");
        eventSender.send(request.toVehicleEventLog(VehicleEventType.GEOFENCE_IN));
        log.info("--------------- 이벤트 퍼블리싱 직후");

        return new ResponseEntity<>(
                new EventResponse("200", "OK", request.getMdn())
                , HttpStatus.OK
        );
    }

    @PostMapping("/api/v1/vehicle/geofence/out")
    public ResponseEntity<EventResponse> geofenceOut(@Valid @RequestBody GeofenceEventRequest request) {
        log.info("지오펜스 IN 컨트롤러 진입 : {} ", request);
        log.info("--------------- 이벤트 퍼블리싱 직전");
        eventSender.send(request.toVehicleEventLog(VehicleEventType.GEOFENCE_OUT));
        log.info("--------------- 이벤트 퍼블리싱 직후");

        return new ResponseEntity<>(
                new EventResponse("200", "OK", request.getMdn())
                , HttpStatus.OK
        );
    }
}
