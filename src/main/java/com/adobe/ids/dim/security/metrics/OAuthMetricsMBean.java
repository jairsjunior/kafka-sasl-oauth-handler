package com.adobe.ids.dim.security.metrics;

public interface OAuthMetricsMBean {
    Integer getCountOfRequestSuccess();
    Integer getCountOfRequestFailedInvalidToken();
    Integer getCountOfRequestFailedExpiredToken();
}
