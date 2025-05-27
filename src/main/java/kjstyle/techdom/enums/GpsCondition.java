package kjstyle.techdom.enums;

import lombok.Getter;

@Getter
public enum GpsCondition {
    // Enum 이름 (Java 코드에서 사용)  - 실제 DB에 저장될 코드 값
    NORMAL("A"),          // 정상
    ABNORMAL("V"),        // 비정상
    NOT_INSTALLED("0"),   // 미장착 (Java에서는 'NOT_INSTALLED'로, DB에는 '0'으로 저장)
    IGNITION_ABNORMAL("P"); // 시동 ON/OFF 시 GPS 비정상

    private final String code; // 실제 DB에 저장될 값을 위한 필드

    GpsCondition(String code) {
        this.code = code;
    }

    // code 값으로 Enum을 찾아주는 정적 팩토리 메서드
    public static GpsCondition fromCode(String code) {
        for (GpsCondition condition : GpsCondition.values()) {
            if (condition.getCode().equals(code)) {
                return condition;
            }
        }
        throw new IllegalArgumentException("Unknown GPS condition code: " + code);
    }
}
