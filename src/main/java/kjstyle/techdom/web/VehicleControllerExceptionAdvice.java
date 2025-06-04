package kjstyle.techdom.web;

import kjstyle.techdom.web.dto.EventResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@ControllerAdvice
public class VehicleControllerExceptionAdvice {
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