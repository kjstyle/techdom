package kjstyle.techdom.web;

import jakarta.validation.Valid;
import kjstyle.techdom.enums.VehicleEventType;
import kjstyle.techdom.web.dto.EventResponse;
import kjstyle.techdom.web.dto.GeofenceEventRequest;
import kjstyle.techdom.web.dto.IgnitionEventRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.coyote.BadRequestException;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class VehicleEventController {

    private final ApplicationEventPublisher eventPublisher;

    @PostMapping("/api/v1/vehicle/ignition")
    public ResponseEntity<EventResponse> ignition(@Valid @RequestBody IgnitionEventRequest request) { // TODO : 응답을 어떻게 해야할지
        log.info("시동 이벤트 수신 : {} ", request);
        log.info("/api/v1/vehicle/ignition received event on controller : {} [ThreadId: {}]", request, Thread.currentThread().getId());
        log.info("--------------- 이벤트 퍼블리싱 직전");
        eventPublisher.publishEvent(request.toVehicleEventLog()); // TODO : 서비스로 빼는게 맞는걸까?
        log.info("--------------- 이벤트 퍼블리싱 직후");
        return new ResponseEntity<>(
                new EventResponse("200", "OK", request.getMdn())
                , HttpStatus.OK
        );
    }

    @PostMapping("/api/v1/vehicle/geofence/in")
    public ResponseEntity<EventResponse> geofenceIn(@Valid @RequestBody GeofenceEventRequest request) {
        log.info("지오펜스 IN 이벤트 수신 : {} ", request);
        log.info("/api/v1/vehicle/geofence/in received event on controller : {} [ThreadId: {}]", request, Thread.currentThread().getId());
        eventPublisher.publishEvent(request.toVehicleEventLog(VehicleEventType.GEOFENCE_IN));
        return new ResponseEntity<>(
                new EventResponse("200", "OK", request.getMdn())
                , HttpStatus.OK
        );
    }

    @PostMapping("/api/v1/vehicle/geofence/out")
    public ResponseEntity<EventResponse> geofenceOut(@Valid @RequestBody GeofenceEventRequest request) {
        log.info("지오펜스 OUT 이벤트 수신 : {} ", request);
        log.info("/api/v1/vehicle/geofence/out received event on controller : {} [ThreadId: {}]", request, Thread.currentThread().getId());
        eventPublisher.publishEvent(request.toVehicleEventLog(VehicleEventType.GEOFENCE_OUT));
        return new ResponseEntity<>(
                new EventResponse("200", "OK", request.getMdn())
                , HttpStatus.OK
        );
    }
}
