package kjstyle.techdom.enums;

/**
 * 차량에서 사용되는 연료 유형을 정의하는 열거형입니다.
 *
 * 다음과 같은 연료 유형을 지원합니다:
 * - GASOLINE: 휘발유를 연료로 사용하는 내연기관 차량
 * - DIESEL: 경유를 연료로 사용하는 내연기관 차량
 * - HYBRID: 전기 모터와 내연기관을 복합적으로 사용하는 하이브리드 차량
 * - ELECTRIC: 전기를 동력원으로 사용하는 전기 자동차
 * - LPG: 액화석유가스를 연료로 사용하는 차량
 */
public enum FuelType {
    GASOLINE,
    DIESEL,
    HYBRID,
    ELECTRIC,
    LPG
}
