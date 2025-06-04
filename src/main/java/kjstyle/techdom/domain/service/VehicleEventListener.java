package kjstyle.techdom.domain.service;

import kjstyle.techdom.domain.repository.entitys.VehicleEventLog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import static java.lang.Thread.sleep;

@Slf4j
@Component
@RequiredArgsConstructor
public class VehicleEventListener {

    private final VehicleEventLogService vehicleEventLogService;

    @Async
    @EventListener
    public void handleEvent(VehicleEventLog eventLog) {
        log.info("--------------- 이벤트 구독 직후");
        // FIXME : 비동기를 확인하기 위한 sleep으로 꼭 제거 필요
        try {
            sleep(500);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
        log.info("--------------- 이벤트 구독 후 슬립 후 핸들러 호출 직전");
        log.info("VehicleEventListener received event: {} [ThreadId: {}]", eventLog, Thread.currentThread().getId());
        vehicleEventLogService.processVehicleEvent(eventLog);
    }
}
