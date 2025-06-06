package kjstyle.techdom.domain.exceptions;

import lombok.Getter;
import lombok.ToString;
import org.springframework.http.HttpStatus;

@Getter
@ToString
public class VehicleEventHandleException extends RuntimeException {

    private String errorCode;
    private HttpStatus httpStatus;

    public VehicleEventHandleException(String message) {
        super(message);
    }
}
