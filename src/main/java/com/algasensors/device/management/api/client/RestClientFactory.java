package com.algasensors.device.management.api.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Component
public class RestClientFactory {

    private final RestClient.Builder builder;
    private final String baseUrl;
    private final Long readTimeout;
    private final Long connectTimeout;

    public RestClientFactory(
            @Autowired RestClient.Builder builder,
            @Value("${rest-client.sensor-monitoring-client.baseUrl}") String baseUrl,
            @Value("${rest-client.sensor-monitoring-client.read-timeout}") Long readTimeout,
            @Value("${rest-client.sensor-monitoring-client.connect-timeout}") Long connectTimeout
    ) {
        this.builder = builder;
        this.baseUrl = baseUrl;
        this.readTimeout = readTimeout;
        this.connectTimeout = connectTimeout;
    }

    public RestClient temperatureMonitoringClient() {
        return builder
                .requestFactory(generateClientHttpRequestFactory(readTimeout, connectTimeout))
                .baseUrl(baseUrl)
                .defaultStatusHandler(HttpStatusCode::isError, (request, response) -> {
                    throw new SensorMonitoringClientBadGatewayException();
                }).build();
    }

    private ClientHttpRequestFactory generateClientHttpRequestFactory(Long readTimeout, Long connectTimeout) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setReadTimeout(Duration.ofMillis(readTimeout));
        factory.setConnectTimeout(Duration.ofMillis(connectTimeout));

        return factory;
    }
}
