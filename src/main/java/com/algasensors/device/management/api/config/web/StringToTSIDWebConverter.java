package com.algasensors.device.management.api.config.web;

import io.hypersistence.tsid.TSID;
import org.springframework.core.convert.converter.Converter;
import org.springframework.lang.NonNull;
import org.springframework.util.Assert;

public class StringToTSIDWebConverter implements Converter<String, TSID> {

    @Override
    public TSID convert(@NonNull String source) {
        Assert.notNull(source, "source must not be null.");
        return TSID.from(source);
    }
}
