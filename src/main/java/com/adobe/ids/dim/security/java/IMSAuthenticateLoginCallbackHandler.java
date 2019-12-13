package com.adobe.ids.dim.security.java;

import org.apache.kafka.common.KafkaException;
import org.apache.kafka.common.security.auth.AuthenticateCallbackHandler;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerTokenCallback;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import java.io.IOException;
import java.util.*;

import com.adobe.ids.dim.security.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IMSAuthenticateLoginCallbackHandler implements AuthenticateCallbackHandler {
    private final Logger log = LoggerFactory.getLogger(IMSAuthenticateLoginCallbackHandler.class);
    private Map < String, String > moduleOptions = null;
    private boolean configured = false;

    @Override
    public void configure(Map < String, ? > map, String saslMechanism, List < AppConfigurationEntry > jaasConfigEntries) {
        log.info("IMSAuthenticateLoginCallbackHandler configure");
        if (!OAuthBearerLoginModule.OAUTHBEARER_MECHANISM.equals(saslMechanism)){
            log.info(String.format("Unexpected SASL mechanism: %s", saslMechanism));
            throw new IllegalArgumentException(String.format("Unexpected SASL mechanism: %s", saslMechanism));
        }
        if (Objects.requireNonNull(jaasConfigEntries).size() < 1 || jaasConfigEntries.get(0) == null){
            log.info(String.format("Must supply exactly 1 non-null JAAS mechanism configuration (size was %d)",
                    jaasConfigEntries.size()));
            throw new IllegalArgumentException(
                    String.format("Must supply exactly 1 non-null JAAS mechanism configuration (size was %d)",
                            jaasConfigEntries.size()));
        }
        this.moduleOptions = Collections.unmodifiableMap((Map < String, String > ) jaasConfigEntries.get(0).getOptions());
        configured = true;
    }

    public boolean isConfigured() {
        return this.configured;
    }

    @Override
    public void close() {}

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        log.info("IMSAuthenticateLoginCallbackHandler handle");
        if (!isConfigured()){
            log.info("Callback handler not configured");
            throw new IllegalStateException("Callback handler not configured");
        }

        log.info("For each callback received on handle");
        for (Callback callback: callbacks) {
            if (callback instanceof OAuthBearerTokenCallback){
                try {
                    log.info("callback is a instance of OAuthBearerTokenCallback");
                    handleCallback((OAuthBearerTokenCallback) callback);
                } catch (KafkaException e) {
                    log.info("on handleCallback");
                    throw new IOException(e.getMessage(), e);
                }
            }else{
                log.info("Callback is not a instance of OAuthBearerTokenCallback", callback.getClass().getName());
                throw new UnsupportedCallbackException(callback);
            }
        }
    }

    private void handleCallback(OAuthBearerTokenCallback callback) {
        log.info("IMSAuthenticateLoginCallbackHandler handleCallback");
        if (callback.token() != null){
            log.info("Callback had a token already");
            throw new IllegalArgumentException("Callback had a token already");
        }

        IMSBearerTokenJwt token = IMSHttpCalls.getIMSToken(moduleOptions);
        if (token == null) {
            log.info("Null token returned from server");
            throw new IllegalArgumentException("Null token returned from server");
        }

        log.info("Retrieved IMS Token: {}", token.toString());
        callback.token(token);
    }

}
