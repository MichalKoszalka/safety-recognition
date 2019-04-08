package com.safety.recognition.client;

import org.springframework.beans.factory.annotation.Value;

class RequestPathBuilder {

    @Value("${crime.api.url}")
    private String crimeApiUrl;

    private boolean firstQueryParamApplied;

    private final StringBuilder stringBuilder;

    RequestPathBuilder() {
        stringBuilder = new StringBuilder();
        stringBuilder.append(crimeApiUrl);
    }

    RequestPathBuilder withMethod(String method) {
        stringBuilder.append(method);
        return this;
    }

    RequestPathBuilder withQueryParam(String paramName, String value) {
        if (firstQueryParamApplied) {
            stringBuilder.append("&");
        } else {
            stringBuilder.append("?");
        }
        stringBuilder.append(paramName).append(value);
        return this;
    }

    String build() {
        return stringBuilder.toString();
    }





}
