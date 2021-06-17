package com.innometrics.gqm_api.dto;

import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class ApiErrorResponse {

    private String path;

    private HttpStatus status;

    private String message;

    private LocalDateTime dateTime;

    @Builder.Default
    private Map<String, String> validationErrors = new HashMap<>();

    public void addError(String field, String message) {
        validationErrors.put(field, message);
    }
}