package com.adobe.ids.dim.security.rest.context;

import com.adobe.ids.dim.security.util.IMSBearerTokenJwt;
import com.adobe.ids.dim.security.rest.config.KafkaOAuthSecurityRestConfig;
import io.confluent.kafkarest.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
        String principalWithResourceType = principal.principalName()+"--"+resourceType;
        log.info("Principal With Resource Type: ", principalWithResourceType);
        if (this.userToContextMap.containsKey(principalWithResourceType)) {
            log.info("has userToContextMap principal: ", principalWithResourceType);
            return this.userToContextMap.get(principalWithResourceType);
        }
        synchronized (principalWithResourceType) {
            log.info("create userToContextMap principal: ", principalWithResourceType);
            final ScalaConsumersContext scalaConsumersContext = new ScalaConsumersContext(kafkaRestConfig);
            final KafkaRestContext context = new DefaultKafkaRestContext(kafkaRestConfig, null, null, null, scalaConsumersContext);
            this.userToContextMap.put(principalWithResourceType, context);
        }
        return this.userToContextMap.get(principalWithResourceType);
    }

    public void clean() {
        log.info("KafkaOAuthRestContextFactory -- clean");
        for (final KafkaRestContext context : this.userToContextMap.values()) {
            context.shutdown();
        }
        this.userToContextMap.clear();
    }
}
