package com.safety.recognition.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestPathBuilder {

    private static final Logger LOG = LoggerFactory.getLogger(RequestPathBuilder.class);

    private boolean firstQueryParamApplied = false;

    private StringBuilder stringBuilder;

    RequestPathBuilder(String crimeApiUrl) {
        stringBuilder = new StringBuilder();
        stringBuilder.append(crimeApiUrl);
    }

    RequestPathBuilder withMethod(String method) {
        stringBuilder.append(method);
        return this;
    }

    RequestPathBuilder withQueryParam(String paramName, String value) {
        if (!firstQueryParamApplied) {
            stringBuilder.append("?");
            firstQueryParamApplied = true;
        } else {
            stringBuilder.append("&");
        }
        stringBuilder.append(paramName).append("=").append(value);
        return this;
    }

    String build() {
        var value = stringBuilder.toString();
        LOG.info(String.format("Generated path: %s", value));
        return value;
    }


}
