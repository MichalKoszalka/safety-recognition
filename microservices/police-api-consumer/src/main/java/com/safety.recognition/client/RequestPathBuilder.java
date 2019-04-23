package com.safety.recognition.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class RequestPathBuilder {

    @Value("${crime.api.url}")
    private String crimeApiUrl;

    private boolean firstQueryParamApplied = false;

    private StringBuilder stringBuilder;

    RequestPathBuilder() {
    }

    RequestPathBuilder newRequest() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(crimeApiUrl);
        return this;
    }

    RequestPathBuilder withMethod(String method) {
        stringBuilder.append(method);
        return this;
    }

    RequestPathBuilder withQueryParam(String paramName, String value) {
        if (!firstQueryParamApplied) {
            stringBuilder.append("?");
        } else {
            firstQueryParamApplied = true;
            stringBuilder.append("&");
        }
        stringBuilder.append(paramName).append("=").append(value);
        return this;
    }

    String build() {
        return stringBuilder.toString();
    }





}
