/*
 * ADOBE CONFIDENTIAL. Copyright 2019 Adobe Systems Incorporated. All Rights Reserved. NOTICE: All information contained
 * herein is, and remains the property of Adobe Systems Incorporated and its suppliers, if any. The intellectual and
 * technical concepts contained herein are proprietary to Adobe Systems Incorporated and its suppliers and are protected
 * by all applicable intellectual property laws, including trade secret and copyright law. Dissemination of this
 * information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 */

package com.adobe.ids.dim.security.java;

import com.adobe.ids.dim.security.metrics.OAuthMetrics;
import com.adobe.ids.dim.security.metrics.OAuthMetricsValidator;
import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.security.auth.AuthenticateCallbackHandler;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerValidatorCallback;
import org.apache.kafka.common.security.oauthbearer.internals.unsecured.OAuthBearerValidationResult;
import org.apache.kafka.common.utils.Time;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import com.adobe.ids.dim.security.util.*;

public class IMSAuthenticateValidatorCallbackHandler implements AuthenticateCallbackHandler {
    private final Logger log = LoggerFactory.getLogger(IMSAuthenticateValidatorCallbackHandler.class);
    private Map < String, String > moduleOptions = null;
    private boolean configured = false;
    private Time time = Time.SYSTEM;

    //Allowed scopes
    private static final String DIM_CORE_SCOPE = "dim.core.services";

    @Override
    public void configure(Map < String, ? > map, String saslMechanism, List < AppConfigurationEntry > jaasConfigEntries) {
        if (!OAuthBearerLoginModule.OAUTHBEARER_MECHANISM.equals(saslMechanism))
            throw new IllegalArgumentException(String.format("Unexpected SASL mechanism: %s", saslMechanism));
        if (Objects.requireNonNull(jaasConfigEntries).size() < 1 || jaasConfigEntries.get(0) == null)
            throw new IllegalArgumentException(
                    String.format("Must supply exactly 1 non-null JAAS mechanism configuration (size was %d)",
                            jaasConfigEntries.size()));
        this.moduleOptions = Collections.unmodifiableMap((Map < String, String > ) jaasConfigEntries.get(0).getOptions());
        configured = true;
        registerMetrics();
    }

    public boolean isConfigured() {
        return this.configured;
    }

    @Override
    public void close() {}

    public void registerMetrics(){
        try{
            MBeanServer platformMBeanServer = ManagementFactory.getPlatformMBeanServer();
            ObjectName objectName = new ObjectName("com.adobe.ids.dim.security.app:name=OAuthMetrics");
            platformMBeanServer.registerMBean(OAuthMetricsValidator.getInstance(), objectName);
        }catch (Exception e){
            log.error("Error on register MBean Server for JMX metrics");
        }
    }

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        if (!isConfigured())
            throw new IllegalStateException("Callback handler not configured");
        for (Callback callback: callbacks) {
            if (callback instanceof OAuthBearerValidatorCallback)
                try {
                    OAuthBearerValidatorCallback validationCallback = (OAuthBearerValidatorCallback) callback;
                    handleCallback(validationCallback);
                } catch (KafkaException e) {
                    throw new IOException(e.getMessage(), e);
                }
            else
                throw new UnsupportedCallbackException(callback);
        }
    }

    private void handleCallback(OAuthBearerValidatorCallback callback)
            throws IllegalArgumentException {
        String accessToken = callback.tokenValue();
        if (accessToken == null)
            throw new IllegalArgumentException("Callback missing required token value");

        IMSBearerTokenJwt token = IMSHttpCalls.validateIMSToken(accessToken, moduleOptions);

        //Check if Token has expired
        long now = time.milliseconds();

        log.debug("Token expiration time: {}", token.lifetimeMs());

        if (now > token.lifetimeMs()) {
            log.debug("Token has expired! Needs refresh");
            OAuthBearerValidationResult.newFailure("Expired Token").throwExceptionIfFailed();
        }

        //Check if we have DIM specific scope in the token or not
        Set<String> scopes = token.scope();

        if (!scopes.contains(DIM_CORE_SCOPE)) {
            OAuthMetricsValidator.getInstance().incCountOfRequestsFailedWithoutACL();
            log.debug("Token doesn't have required scopes! We cannot accept this token");
            log.debug("Required scope is: {}", DIM_CORE_SCOPE);
            log.debug("Token has following scopes: {}", scopes);
            OAuthBearerValidationResult.newFailure("Required scope missing").throwExceptionIfFailed();
        }

        log.debug("Validated IMS Token: {}", token.toString());
        callback.token(token);
    }
}