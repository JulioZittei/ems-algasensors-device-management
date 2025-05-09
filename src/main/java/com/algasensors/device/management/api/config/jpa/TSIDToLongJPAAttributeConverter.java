package com.algasensors.device.management.api.config.jpa;

import io.hypersistence.tsid.TSID;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

@Converter(autoApply = true)
public class TSIDToLongJPAAttributeConverter implements AttributeConverter<TSID, Long> {

    @Override
    public Long convertToDatabaseColumn(@NonNull TSID attribute) {
        Assert.notNull(attribute, "attribute must not be null.");
        return attribute.toLong();
    }

    @Override
    public TSID convertToEntityAttribute(@NonNull Long dbData) {
        Assert.notNull(dbData, "dbData must not be null.");
        return TSID.from(dbData);
    }

}
