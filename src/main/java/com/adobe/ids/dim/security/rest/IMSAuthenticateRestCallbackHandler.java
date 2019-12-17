package com.adobe.ids.dim.security.rest;

import com.adobe.ids.dim.security.util.IMSBearerTokenJwt;
import com.adobe.ids.dim.security.util.OAuthRestProxyUtil;
import kafka.common.KafkaException;
import org.apache.kafka.common.security.auth.AuthenticateCallbackHandler;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule;
import org.apache.kafka.common.security.oauthbearer.OAuthBearerTokenCallback;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.security.auth.login.AppConfigurationEntry;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class IMSAuthenticateRestCallbackHandler implements AuthenticateCallbackHandler {
    private final Logger log = LoggerFactory.getLogger(IMSAuthenticateRestCallbackHandler.class);
    private Map < String, String > moduleOptions = null;
    private boolean configured = false;

    @Override
    public void configure(Map<String, ?> map, String s, List<AppConfigurationEntry> list) {
        if (!OAuthBearerLoginModule.OAUTHBEARER_MECHANISM.equals(s))
            throw new IllegalArgumentException(String.format("Unexpected SASL mechanism: %s", s));
        if (Objects.requireNonNull(list).size() < 1 || list.get(0) == null)
            throw new IllegalArgumentException(
                    String.format("Must supply exactly 1 non-null JAAS mechanism configuration (size was %d)",
                            list.size()));
        this.moduleOptions = Collections.unmodifiableMap((Map < String, String > ) list.get(0).getOptions());
        configured = true;
    }

    public boolean isConfigured() {
        return this.configured;
    }

    @Override
    public void close() {}

    @Override
    public void handle(Callback[] callbacks) throws IOException, UnsupportedCallbackException {
        if (!isConfigured())
            throw new IllegalStateException("Callback handler not configured");

        for (Callback callback: callbacks) {
            if (callback instanceof OAuthBearerTokenCallback)
                try {
                    handleCallback((OAuthBearerTokenCallback) callback);
                } catch (KafkaException e) {
                    throw new IOException(e.getMessage(), e);
                }
            else
                throw new UnsupportedCallbackException(callback);
        }
    }

    private void handleCallback(OAuthBearerTokenCallback callback) {
        if (callback.token() != null)
            throw new IllegalArgumentException("Callback had a token already");

        String tokenCode = moduleOptions.get("ims.access.token");
        if(tokenCode == null){
            throw new IllegalArgumentException("Null token passed at jaasConfig file");
        }
        IMSBearerTokenJwt token;
        try {
             token = OAuthRestProxyUtil.getIMSBearerTokenJwtFromBearer(tokenCode);
        } catch (IOException e){
            e.printStackTrace();
            throw new IllegalArgumentException("Null token passed at jaasConfig file");
        }

        log.debug("Jaas file IMS Token: {}", tokenCode);
        callback.token(token);
    }
}
