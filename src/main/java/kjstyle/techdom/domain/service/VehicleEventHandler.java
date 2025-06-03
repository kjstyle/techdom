package kjstyle.techdom.domain.service;

import kjstyle.techdom.domain.repository.entitys.VehicleEventLog;
import kjstyle.techdom.enums.VehicleEventType;

public interface VehicleEventHandler {

    VehicleEventType getEventType();

    void handle(VehicleEventLog eventLog);
}