package kjstyle.techdom.web;

import jakarta.validation.Valid;
import kjstyle.techdom.web.dto.EventResponse;
import kjstyle.techdom.web.dto.IgnitionEventRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequiredArgsConstructor
public class IgnitionEventController {

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

    // 유효성 검사 실패 시 발생하는 MethodArgumentNotValidException을 처리하는 핸들러
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<EventResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        List<String> errors = ex.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.toList());

        String mdn = (ex.getBindingResult().getFieldValue("mdn") instanceof String) ?
                (String) ex.getBindingResult().getFieldValue("mdn") : null;

        return new ResponseEntity<>(
                new EventResponse("400", "요청 파라미터 유효성 검사에 실패했습니다.", mdn, errors),
                HttpStatus.BAD_REQUEST
        );
    }

    @ExceptionHandler(Exception.class)
    public EventResponse handleException(Exception e) {
        log.error("알 수 없는 에러 : {}", e.getMessage());
        return new EventResponse("500", "INTERNAL SERVER ERROR", e.getMessage());
    }
}
