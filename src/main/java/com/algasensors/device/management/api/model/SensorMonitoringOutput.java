package com.algasensors.device.management.api.model;

import io.hypersistence.tsid.TSID;

import java.time.OffsetDateTime;

public record SensorMonitoringOutput(
        TSID sensorId,
        Double lastTemperature,
        OffsetDateTime updatedAt,
        Boolean enabled
) {
}
