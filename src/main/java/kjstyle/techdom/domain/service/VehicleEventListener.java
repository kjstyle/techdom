package kjstyle.techdom.domain.service;

import kjstyle.techdom.domain.repository.entitys.VehicleEventLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class VehicleEventListener {

    private final VehicleEventLogService vehicleEventLogService;

    @Async
    @EventListener
    public void handleEvent(VehicleEventLog eventLog) {
        vehicleEventLogService.processVehicleEvent(eventLog);
    }
}
