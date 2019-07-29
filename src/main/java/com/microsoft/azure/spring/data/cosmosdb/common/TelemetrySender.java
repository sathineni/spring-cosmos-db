/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.data.cosmosdb.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.NonNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.util.Assert;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON;

@Slf4j
public class TelemetrySender {

    private static final String PROPERTY_INSTALLATION_ID = "installationId";

    private static final String PROPERTY_VERSION = "version";

    private static final String PROPERTY_SERVICE_NAME = "serviceName";

    private static final String PROJECT_INFO = "spring-data-cosmosdb/" + PropertyLoader.getProjectVersion();

    private static final String TELEMETRY_TARGET_URL = "https://dc.services.visualstudio.com/v2/track";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private static final int RETRY_LIMIT = 3; // Align the retry times with sdk

    private ResponseEntity<String> executeRequest(final TelemetryEventData eventData) {
        final HttpHeaders headers = new HttpHeaders();

        headers.add(HttpHeaders.CONTENT_TYPE, APPLICATION_JSON.toString());

        try {
            final RestTemplate restTemplate = new RestTemplate();
            final HttpEntity<String> body = new HttpEntity<>(MAPPER.writeValueAsString(eventData), headers);

            return restTemplate.exchange(TELEMETRY_TARGET_URL, HttpMethod.POST, body, String.class);
        } catch (JsonProcessingException | HttpClientErrorException ignore) {
            log.warn("Failed to exchange telemetry request, {}.", ignore.getMessage());
        }

        return null;
    }

    private void sendTelemetryData(@NonNull TelemetryEventData eventData) {
        ResponseEntity<String> response = null;

        for (int i = 0; i < RETRY_LIMIT; i++) {
            response = executeRequest(eventData);

            if (response != null && response.getStatusCode() == HttpStatus.OK) {
                return;
            }
        }

        if (response != null && response.getStatusCode() != HttpStatus.OK) {
            log.warn("Failed to send telemetry data, response status code {}.", response.getStatusCode().toString());
        }
    }

    public void send(String name) {
        Assert.hasText(name, "Event name should contain text.");

        sendTelemetryData(new TelemetryEventData(name, getProperties()));
    }

    private Map<String, String> getProperties() {
        final Map<String, String> properties = new HashMap<>();

        properties.put(PROPERTY_VERSION, PROJECT_INFO);
        properties.put(PROPERTY_SERVICE_NAME, "cosmosdb");
        properties.put(PROPERTY_INSTALLATION_ID, MacAddress.getHashMac());

        return properties;
    }
}

