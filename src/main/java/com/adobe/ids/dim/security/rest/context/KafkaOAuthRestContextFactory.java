package com.adobe.ids.dim.security.rest.context;

import com.adobe.ids.dim.security.util.IMSBearerTokenJwt;
import com.adobe.ids.dim.security.rest.config.KafkaOAuthSecurityRestConfig;
import io.confluent.kafkarest.*;
import io.confluent.kafkarest.extension.KafkaRestContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Principal;
import java.util.Map;
import java.util.HashMap;

public class KafkaOAuthRestContextFactory {

    private static final Logger log = LoggerFactory.getLogger(KafkaOAuthRestContextFactory.class);
    private static final KafkaOAuthRestContextFactory instance = new KafkaOAuthRestContextFactory();
    private final Map<String, KafkaRestContext> userToContextMap;

    private KafkaOAuthRestContextFactory() {
        this.userToContextMap = new HashMap<String, KafkaRestContext>();
    }

    public static KafkaOAuthRestContextFactory getInstance() {
        return KafkaOAuthRestContextFactory.instance;
    }

    public KafkaRestContext getContext(final IMSBearerTokenJwt principal, final KafkaOAuthSecurityRestConfig kafkaRestConfig, final String resourceType, final boolean tokenAuth) {
        log.info("KafkaOAuthRestContextFactory -- getContext");
        if (this.userToContextMap.containsKey(principal.principalName())) {
            log.info("has userToContextMap principal: ", principal.principalName());
            return this.userToContextMap.get(principal.principalName());
        }
        synchronized (principal.principalName().intern()) {
            log.info("create userToContextMap principal: ", principal.principalName());
            final ScalaConsumersContext scalaConsumersContext = KafkaRestContextProvider.getDefaultContext().getScalaConsumersContext();
            final KafkaRestContext context = new DefaultKafkaRestContext(kafkaRestConfig, null, null, null, scalaConsumersContext);
            this.userToContextMap.put(principal.principalName(), context);
        }
        return this.userToContextMap.get(principal.principalName());
    }

    public void clean() {
        log.info("KafkaOAuthRestContextFactory -- clean");
        for (final KafkaRestContext context : this.userToContextMap.values()) {
            context.shutdown();
        }
        this.userToContextMap.clear();
    }
}
