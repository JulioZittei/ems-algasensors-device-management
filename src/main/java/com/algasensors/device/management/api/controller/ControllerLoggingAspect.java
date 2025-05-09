package com.algasensors.device.management.api.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.lang.reflect.Parameter;

@Slf4j
@Aspect
@Component
public class ControllerLoggingAspect {

    private final ObjectMapper objectMapper;

    public ControllerLoggingAspect(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Before("@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.PatchMapping) || " +
            "@annotation(org.springframework.web.bind.annotation.DeleteMapping)")
    public void logRequest(JoinPoint joinPoint) {
        try {
            MethodSignature signature = (MethodSignature) joinPoint.getSignature();
            String methodName = signature.getMethod().getName();
            String className = joinPoint.getTarget().getClass().getSimpleName();
            String uri = ServletUriComponentsBuilder.fromCurrentRequest().build().toUriString();

            Object[] args = joinPoint.getArgs();
            Parameter[] parameters = signature.getMethod().getParameters();

            StringBuilder stringBuilder = new StringBuilder(String.format("[%s::%s] [%s] ", className, methodName, uri));

            for (int i = 0; i < parameters.length; i++) {
                if (parameters[i].isAnnotationPresent(RequestBody.class)) {
                    String jsonBody = objectMapper.writeValueAsString(args[i]);
                    stringBuilder.append(String.format("RequestBody: %s ", jsonBody));
                } else if (parameters[i].isAnnotationPresent(RequestHeader.class)) {
                    String jsonBody = objectMapper.writeValueAsString(args[i]);
                    stringBuilder.append(String.format("Headers: %s ", jsonBody));
                }
            }
            log.info(stringBuilder.toString());
        } catch (Exception e) {
            log.error("Falha ao logar @RequestBody e @RequestHeader: {}", e.getMessage());
        }
    }

    @AfterReturning(
            pointcut = "@annotation(org.springframework.web.bind.annotation.GetMapping) || " +
                    "@annotation(org.springframework.web.bind.annotation.PostMapping) || " +
                    "@annotation(org.springframework.web.bind.annotation.PatchMapping) || " +
                    "@annotation(org.springframework.web.bind.annotation.PutMapping) || " +
                    "@annotation(org.springframework.web.bind.annotation.DeleteMapping)",
            returning = "result"
    )
    public void logResponse(JoinPoint joinPoint, Object result) throws JsonProcessingException {
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        String methodName = signature.getMethod().getName();
        String className = joinPoint.getTarget().getClass().getSimpleName();
        String uri = ServletUriComponentsBuilder.fromCurrentRequest().build().toUriString();

        String resultJson = (result instanceof String || result instanceof Number || result instanceof Boolean)
                ? result.toString()
                : objectMapper.writeValueAsString(result);

        boolean isVoid = void.class.equals(signature.getMethod().getReturnType());

        if (isVoid) {
            log.info("[{}::{}] [{}] Sem response", className, methodName, uri);
        } else {
            log.info("[{}::{}] [{}] Response: {}", className, methodName, uri, resultJson);
        }
    }
}