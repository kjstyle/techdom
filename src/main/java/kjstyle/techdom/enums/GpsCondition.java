package kjstyle.techdom.enums;

import lombok.Getter;

/**
 * GPS 장치의 상태를 표현하는 열거형 클래스입니다.
 * 각 상태는 데이터베이스에 저장될 특정 코드값과 매핑됩니다.
 */
@Getter
public enum GpsCondition {
    /**
     * NORMAL("A") - GPS 장치가 정상 작동중인 상태
     * ABNORMAL("V") - GPS 장치가 비정상 작동중인 상태
     * NOT_INSTALLED("0") - GPS 장치가 미설치된 상태
     * ABNORMAL_ON_IGNITION("P") - 시동 ON/OFF 시 GPS 장치가 비정상인 상태
     */
    NORMAL("A"),
    ABNORMAL("V"),
    NOT_INSTALLED("0"),
    ABNORMAL_ON_IGNITION("P");

    /**
     * 데이터베이스에 저장될 GPS 상태 코드값
     */
    private final String code;

    /**
     * GPS 상태 열거형 생성자입니다.
     *
     * @param code 데이터베이스에 저장될 상태 코드값
     */
    GpsCondition(String code) {
        this.code = code;
    }

    /**
     * 데이터베이스에 저장된 코드값으로 GPS 상태를 조회하는 메서드입니다.
     *
     * @param code GPS 상태 코드값
     * @return 해당 코드값에 매핑되는 GPS 상태
     * @throws IllegalArgumentException 유효하지 않은 코드값이 입력된 경우
     */
    public static GpsCondition fromCode(String code) {
        for (GpsCondition condition : GpsCondition.values()) {
            if (condition.getCode().equals(code)) {
                return condition;
            }
        }
        throw new IllegalArgumentException("Unknown GPS condition code: " + code);
    }
}
