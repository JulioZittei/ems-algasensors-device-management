package com.algasensors.device.management.api.controller;

import com.algasensors.device.management.api.client.SensorMonitoringClient;
import com.algasensors.device.management.api.model.SensorDetailOutput;
import com.algasensors.device.management.api.model.SensorInput;
import com.algasensors.device.management.api.model.SensorMonitoringOutput;
import com.algasensors.device.management.api.model.SensorOutput;
import com.algasensors.device.management.common.IdGenerator;
import com.algasensors.device.management.domain.model.Sensor;
import com.algasensors.device.management.domain.model.SensorId;
import com.algasensors.device.management.domain.repository.SensorRepository;
import io.hypersistence.tsid.TSID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.beans.PropertyDescriptor;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Slf4j
@RestController
@RequestMapping("/api/sensors")
@RequiredArgsConstructor
public class SensorController {

    private final SensorRepository sensorRepository;
    private final SensorMonitoringClient sensorMonitoringClient;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public PagedModel<SensorOutput> search(Pageable pageable, @RequestHeader Map<String, Object> headers) {
        var pageSensor = (sensorRepository.findAll(pageable));

        return new PagedModel<>(pageSensor.map(this::convertToOutput));
    }

    @GetMapping("{sensorId}/detail")
    public SensorDetailOutput getOneWithDetail(@PathVariable("sensorId") TSID sensorId, @RequestHeader Map<String, Object> headers) {
        var sensor = sensorRepository.findById(new SensorId(sensorId)).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );

        var sensorMonitoring = sensorMonitoringClient.getDetail(sensorId);

        return convertToOutput(sensor, sensorMonitoring);
    }

    @GetMapping("{sensorId}")
    public SensorOutput getOne(@PathVariable("sensorId") TSID sensorId, @RequestHeader Map<String, Object> headers) {
        var sensor = sensorRepository.findById(new SensorId(sensorId)).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );

        return convertToOutput(sensor);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public SensorOutput create(@RequestBody SensorInput sensorInput, @RequestHeader Map<String, Object> headers) {
        var newSensor = Sensor.builder()
                .id(new SensorId(IdGenerator.generateTSID()))
                .name(sensorInput.name())
                .ip(sensorInput.ip())
                .location(sensorInput.location())
                .protocol(sensorInput.protocol())
                .model(sensorInput.model())
                .enabled(false)
                .build();

        newSensor = sensorRepository.saveAndFlush(newSensor);

        return convertToOutput(newSensor);
    }

    @PutMapping("{sensorId}")
    @ResponseStatus(HttpStatus.OK)
    public SensorOutput update(@PathVariable("sensorId") TSID sensorId, @RequestBody SensorInput sensorInput, @RequestHeader Map<String, Object> headers) {
        var sensor = sensorRepository.findById(new SensorId(sensorId)).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );

        BeanUtils.copyProperties(sensorInput, sensor, "id", "enabled");
        sensorRepository.save(sensor);

        return convertToOutput(sensor);
    }


    @PatchMapping("{sensorId}")
    @ResponseStatus(HttpStatus.OK)
    public SensorOutput partialUpdate(@PathVariable("sensorId") TSID sensorId, @RequestBody SensorInput sensorInput, @RequestHeader Map<String, Object> headers) {
        var sensor = sensorRepository.findById(new SensorId(sensorId)).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );

        BeanUtils.copyProperties(sensorInput, sensor, getNullPropertyNames(sensorInput));
        sensorRepository.save(sensor);

        return convertToOutput(sensor);
    }

    @DeleteMapping("{sensorId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable("sensorId") TSID sensorId, @RequestHeader Map<String, Object> headers) {
        var sensor = sensorRepository.findById(new SensorId(sensorId)).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );

        sensorRepository.delete(sensor);
        sensorMonitoringClient.disableMonitoring(sensorId);
    }

    @PutMapping("{sensorId}/enable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void enable(@PathVariable("sensorId") TSID sensorId, @RequestHeader Map<String, Object> headers) {
        var sensor = sensorRepository.findById(new SensorId(sensorId)).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );

        sensor.setEnabled(true);
        sensorRepository.save(sensor);
        sensorMonitoringClient.enableMonitoring(sensorId);
    }

    @DeleteMapping("{sensorId}/enable")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void disable(@PathVariable("sensorId") TSID sensorId, @RequestHeader Map<String, Object> headers) {
        var sensor = sensorRepository.findById(new SensorId(sensorId)).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND)
        );

        sensor.setEnabled(false);
        sensorRepository.save(sensor);
        sensorMonitoringClient.disableMonitoring(sensorId);
    }

    private SensorOutput convertToOutput(Sensor sensor) {
        return new SensorOutput(
                sensor.getIdAsTSID(),
                sensor.getName(),
                sensor.getIp(),
                sensor.getLocation(),
                sensor.getProtocol(),
                sensor.getModel(),
                sensor.getEnabled()
        );
    }

    private SensorDetailOutput convertToOutput(Sensor sensor, SensorMonitoringOutput monitoring) {
        return new SensorDetailOutput(
                convertToOutput(sensor),
                monitoring
        );
    }


    /**
     * Identifica todas as propriedades nulas em um objeto
     *
     * @param source O objeto para verificar propriedades nulas
     * @return Array de nomes de propriedades que são nulas
     */
    private String[] getNullPropertyNames(Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        PropertyDescriptor[] pds = src.getPropertyDescriptors();

        Set<String> nullFields = new HashSet<>();
        for (PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());

            if (srcValue == null) {
                nullFields.add(pd.getName());
            }
        }

        return nullFields.toArray(new String[0]);
    }
}
