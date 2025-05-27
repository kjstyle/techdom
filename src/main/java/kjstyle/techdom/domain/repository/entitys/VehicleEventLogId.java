package kjstyle.techdom.domain.repository.entitys;

import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.OffsetDateTime;

@NoArgsConstructor
@EqualsAndHashCode
public class VehicleEventLogId implements Serializable {
    private OffsetDateTime eventTimestampUtc;
    private String mdn;
}
