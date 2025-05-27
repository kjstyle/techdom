package kjstyle.techdom.domain.repository.entitys;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import kjstyle.techdom.enums.GpsCondition;

@Converter(autoApply = true)
public class GpsConditionConverter implements AttributeConverter<GpsCondition, String> {
    @Override
    public String convertToDatabaseColumn(GpsCondition attribute) {
        if (attribute == null) {
            return null;
        }
        return attribute.getCode();
    }

    @Override
    public GpsCondition convertToEntityAttribute(String dbData) {
        if (dbData == null) {
            return null;
        }
        return GpsCondition.fromCode(dbData);
    }
}