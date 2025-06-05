package com.algasensors.device.management.api.model;

public record SensorDetailOutput(
        SensorOutput sensor,
        SensorMonitoringOutput monitoring
) {
}
