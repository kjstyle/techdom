package kjstyle.techdom.enums;

/**
 * 차량에서 발생하는 다양한 이벤트 유형을 정의하는 열거형
 */
public enum VehicleEventType {
    /**
     * 차량 운행 시작을 나타내는 이벤트
     */
    CAR_START,
    /**
     * 차량 운행 종료를 나타내는 이벤트
     */
    CAR_STOP,
    /**
     * 지오펜스(가상 경계) 진입을 나타내는 이벤트
     */
    GEOFENCE_IN,

    /**
     * 지오펜스(가상 경계) 이탈을 나타내는 이벤트
     */
    GEOFENCE_OUT,
    /**
     * 주기적인 차량 상태 보고 이벤트
     */
    PERIODIC_REPORT,
    /**
     * 차량 시동이 켜진 상태를 나타내는 이벤트
     */
    IGNITION_ON,
    /**
     * 차량 시동이 꺼진 상태를 나타내는 이벤트
     */
    IGNITION_OFF,
    /**
     * 긴급 상황 발생을 알리는 이벤트
     */
    SOS,
    /**
     * 배터리 잔량 부족을 알리는 이벤트
     */
    BATTERY_LOW
}
