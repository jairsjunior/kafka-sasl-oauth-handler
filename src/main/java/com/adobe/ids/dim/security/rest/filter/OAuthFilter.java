package com.adobe.ids.dim.security.rest.filter;

import com.adobe.ids.dim.security.util.*;
import com.adobe.ids.dim.security.rest.config.KafkaOAuthSecurityRestConfig;
import com.adobe.ids.dim.security.rest.context.KafkaOAuthRestContextFactory;
import io.confluent.kafkarest.KafkaRestContext;
import io.confluent.kafkarest.extension.KafkaRestContextProvider;
import io.confluent.kafkarest.resources.v2.ConsumersResource;
import io.confluent.rest.RestConfigException;
import io.confluent.rest.exceptions.RestNotAuthorizedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Priority;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerRequestFilter;
import javax.ws.rs.container.ResourceInfo;
import javax.ws.rs.core.Context;
import java.io.IOException;
import java.security.Principal;
import java.util.Optional;

@Priority(5000)
public class OAuthFilter implements ContainerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(OAuthFilter.class);
    private final KafkaOAuthSecurityRestConfig oauthSecurityRestConfig;
    private final String AUTHENTICATION_PREFIX = "Bearer";
    @Context
    ResourceInfo resourceInfo;

    public OAuthFilter(final KafkaOAuthSecurityRestConfig oauthSecurityRestConfig) {
        log.info("Constructor of OAuthFilter");
        this.oauthSecurityRestConfig = oauthSecurityRestConfig;
    }

    public void filter(ContainerRequestContext containerRequestContext) throws IOException {
        log.info("Filter of OAuthFilter");
        if (containerRequestContext.getSecurityContext() != null) {
            final String resourceType = this.getResourceType(containerRequestContext);
            log.info("ResourceType: " + resourceType);
            final IMSBearerTokenJwt principal = getBearerInformation(containerRequestContext);
            log.info("Principal: " + principal.toString());
            final KafkaRestContext context = this.getKafkaRestContext(resourceType, principal);
            log.info("Context: " + context.toString());
            KafkaRestContextProvider.setCurrentContext(context);
        }
    }

    private IMSBearerTokenJwt getBearerInformation(ContainerRequestContext containerRequestContext) throws IOException {
        String authorizationHeader = containerRequestContext.getHeaderString("Authorization");
        if(authorizationHeader == null){
            throw new RestNotAuthorizedException("Authorization Bearer token not sent", 40002);
        }
        if (authorizationHeader.startsWith(AUTHENTICATION_PREFIX)) {
            String bearer = authorizationHeader.substring(AUTHENTICATION_PREFIX.length()).trim();
            return OAuthRestProxyUtil.getIMSBearerTokenJwtFromBearer(bearer);
        }else{
            throw new RestNotAuthorizedException("Authorization Bearer sent not starting with " + AUTHENTICATION_PREFIX, 40004);
        }
    }

    private KafkaRestContext getKafkaRestContext(final String resourceType, final IMSBearerTokenJwt principal) throws IOException {
        log.info("getKafkaRestContext");
        final KafkaRestContext context;
        final KafkaOAuthSecurityRestConfig bearerTokenKafkaRestConfig;
        if (principal instanceof IMSBearerTokenJwt) {
            log.info("principal is instance of IMSBearerTokenJwt");
            if(!OAuthRestProxyUtil.validateExpiration(principal)){
                throw new RestNotAuthorizedException("Bearer token is expired", 40005);
            }
            try {
                log.info("create of bearerTokenKafkaRestConfig");
                bearerTokenKafkaRestConfig = new KafkaOAuthSecurityRestConfig(this.oauthSecurityRestConfig.getOriginalProperties(), principal);
            }
            catch (RestConfigException e) {
                log.info("RestConfigException");
                throw new IOException(e);
            }
            log.info("Get context using Factory");
            context = KafkaOAuthRestContextFactory.getInstance().getContext(principal, bearerTokenKafkaRestConfig, resourceType, true);
        } else {
            log.info("principal is not a instance of IMSBearerTokenJwt");
            throw new IOException("Principal is not a instance of IMSBearerTokenJwt");
        }
        log.info("context: " + context.toString());
        return context;
    }

    private String getResourceType(final ContainerRequestContext requestContext) {
        log.info("getResourceType");
        if (ConsumersResource.class.equals(this.resourceInfo.getResourceClass()) || io.confluent.kafkarest.resources.ConsumersResource.class.equals(this.resourceInfo.getResourceClass())) {
            log.info("consumer");
            return "consumer";
        }
        if (requestContext.getMethod().equals("POST")) {
            log.info("producer");
            return "producer";
        }
        log.info("admin");
        return "admin";
    }
}
