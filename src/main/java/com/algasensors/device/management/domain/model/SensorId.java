package com.algasensors.device.management.domain.model;

import io.hypersistence.tsid.TSID;
import jakarta.persistence.Embeddable;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.util.Assert;

import java.io.Serializable;

@Getter
@Embeddable
@EqualsAndHashCode
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SensorId implements Serializable {

    private TSID value;

    private static final String MUST_NOT_BE_NULL = "value must not be null.";

     public SensorId(TSID value) {
         Assert.notNull(value, MUST_NOT_BE_NULL);
         this.value = value;
     }

    public SensorId(Long value) {
        Assert.notNull(value, MUST_NOT_BE_NULL);
        this.value = TSID.from(value);
    }

    public SensorId(String value) {
        Assert.notNull(value, MUST_NOT_BE_NULL);
        this.value = TSID.from(value);
    }

    @Override
    public String toString() {
        return value.toString();
    }

    public Long toLong() {
        return value.toLong();
    }
}
