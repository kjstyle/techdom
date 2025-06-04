package kjstyle.techdom.domain.entitys;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import kjstyle.techdom.enums.GpsCondition;

/**
 * GPS 상태값을 데이터베이스 컬럼과 엔티티 속성 간에 변환하는 컨버터 클래스입니다.
 * GPS 상태 열거형(GpsCondition)을 문자열로 변환하여 저장하고,
 * 데이터베이스의 문자열을 다시 GPS 상태 열거형으로 변환하는 기능을 제공합니다.
 */
@Converter(autoApply = true)
public class GpsConditionConverter implements AttributeConverter<GpsCondition, String> {
    /**
     * GPS 상태 열거형을 데이터베이스 컬럼 값(문자열)으로 변환합니다.
     *
     * @param attribute 변환할 GPS 상태 열거형
     * @return 데이터베이스에 저장될 문자열 코드값
     */
    @Override
    public String convertToDatabaseColumn(GpsCondition attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getCode();
    }

    /**
     * 데이터베이스 컬럼 값(문자열)을 GPS 상태 열거형으로 변환합니다.
     *
     * @param dbData 데이터베이스에서 읽어온 문자열 코드값
     * @return 변환된 GPS 상태 열거형
     */
    @Override
    public GpsCondition convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return GpsCondition.fromCode(dbData);
    }
}