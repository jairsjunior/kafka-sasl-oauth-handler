package com.adobe.ids.dim.security.rest.filter;

import com.adobe.ids.dim.security.exception.IMSException;
import com.adobe.ids.dim.security.exception.IMSRestException;
import com.adobe.ids.dim.security.metrics.OAuthMetrics;
import com.adobe.ids.dim.security.util.*;
import com.adobe.ids.dim.security.rest.config.KafkaOAuthSecurityRestConfig;
import com.adobe.ids.dim.security.rest.context.KafkaOAuthRestContextFactory;
import io.confluent.kafkarest.KafkaRestContext;
import io.confluent.kafkarest.extension.KafkaRestContextProvider;
import io.confluent.kafkarest.resources.v2.ConsumersResource;
import io.confluent.rest.RestConfigException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import java.io.IOException;

@Priority(1000)
public class OAuthFilter implements ContainerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(OAuthFilter.class);
    private final KafkaOAuthSecurityRestConfig oauthSecurityRestConfig;
    private final String AUTHENTICATION_PREFIX = "Bearer";
    @Context
    ResourceInfo resourceInfo;

    public OAuthFilter(final KafkaOAuthSecurityRestConfig oauthSecurityRestConfig) {
        log.debug("Constructor of OAuthFilter");
        this.oauthSecurityRestConfig = oauthSecurityRestConfig;
    }

    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        log.debug("Filter of OAuthFilter");
        if (containerRequestContext.getSecurityContext() != null) {
            final String resourceType = this.getResourceType(containerRequestContext);
            log.debug("ResourceType: " + resourceType);
            final IMSBearerTokenJwt principal = getBearerInformation(containerRequestContext);
            log.debug("Principal: " + principal.toString());
            final KafkaRestContext context = this.getKafkaRestContext(resourceType, principal);
            log.debug("Context: " + context.toString());
            KafkaRestContextProvider.setCurrentContext(context);
        }
    }

    private IMSBearerTokenJwt getBearerInformation(ContainerRequestContext containerRequestContext) throws IOException {
        String authorizationHeader = containerRequestContext.getHeaderString("Authorization");
        if(authorizationHeader == null){
            OAuthMetrics.getInstance().incCountOfRequestFailedInvalidToken();
            throw new IMSRestException(IMSRestException.BEARER_TOKEN_NOT_SENT_CODE, IMSRestException.BEARER_TOKEN_NOT_SENT_MSG);
        }
        if (authorizationHeader.startsWith(AUTHENTICATION_PREFIX)) {
            String bearer = authorizationHeader.substring(AUTHENTICATION_PREFIX.length()).trim();
            return OAuthRestProxyUtil.getIMSBearerTokenJwtFromBearer(bearer);
        }else{
            OAuthMetrics.getInstance().incCountOfRequestFailedInvalidToken();
            throw new IMSRestException(IMSRestException.BEARER_SENT_NOT_STARTING_WITH_PREFIX_CODE, IMSRestException.BEARER_SENT_NOT_STARTING_WITH_PREFIX_MSG + AUTHENTICATION_PREFIX);
        }
    }

    private KafkaRestContext getKafkaRestContext(final String resourceType, final IMSBearerTokenJwt principal) throws IOException {
        log.debug("getKafkaRestContext");
        final KafkaRestContext context;
        final KafkaOAuthSecurityRestConfig bearerTokenKafkaRestConfig;
        if (principal instanceof IMSBearerTokenJwt) {
            log.debug("principal is instance of IMSBearerTokenJwt");
            if(!OAuthRestProxyUtil.validateExpiration(principal)){
                OAuthMetrics.getInstance().incCountOfRequestsFailedExpiredToken();
                throw new IMSRestException(IMSRestException.BEARER_TOKEN_EXPIRED_CODE, IMSRestException.BEARER_TOKEN_EXPIRED_MSG);
            }
            try {
                log.debug("create of bearerTokenKafkaRestConfig");
                bearerTokenKafkaRestConfig = new KafkaOAuthSecurityRestConfig(this.oauthSecurityRestConfig.getOriginalProperties(), principal);
            }
            catch (RestConfigException e) {
                log.debug("RestConfigException");
                throw new IOException(e);
            }
            log.debug("Get context using Factory");
            context = KafkaOAuthRestContextFactory.getInstance().getContext(principal, bearerTokenKafkaRestConfig, resourceType, true);
            OAuthMetrics.getInstance().incCountOfRequestSuccess();
        } else {
            OAuthMetrics.getInstance().incCountOfRequestFailedInvalidToken();
            log.debug("principal is not a instance of IMSBearerTokenJwt");
            throw new IMSRestException(IMSRestException.BEARER_IS_NOT_INSTANCE_IMS_JWT_CODE, IMSRestException.BEARER_IS_NOT_INSTANCE_IMS_JWT_MSG);
        }
        log.debug("context: " + context.toString());
        return context;
    }

    private String getResourceType(final ContainerRequestContext requestContext) {
        log.debug("getResourceType");
        if (ConsumersResource.class.equals(this.resourceInfo.getResourceClass()) || io.confluent.kafkarest.resources.ConsumersResource.class.equals(this.resourceInfo.getResourceClass())) {
            log.debug("consumer");
            return "consumer";
        }
        if (requestContext.getMethod().equals("POST")) {
            log.debug("producer");
            return "producer";
        }
        log.debug("admin");
        return "admin";
    }
}
