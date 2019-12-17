package com.adobe.ids.dim.security.rest.config;

import com.adobe.ids.dim.security.util.IMSBearerTokenJwt;
import io.confluent.kafkarest.KafkaRestConfig;
import io.confluent.rest.RestConfigException;
import io.confluent.kafkarest.SystemTime;
import org.apache.kafka.common.config.ConfigDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public final class KafkaOAuthSecurityRestConfig extends KafkaRestConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaOAuthSecurityRestConfig.class);
    private static final ConfigDef configDef = createBaseConfigDef();

    private IMSBearerTokenJwt jwtToken;

    public KafkaOAuthSecurityRestConfig(final Properties props, final IMSBearerTokenJwt jwtToken) throws RestConfigException {
        super(KafkaOAuthSecurityRestConfig.configDef, props, new SystemTime());
        log.debug("KafkaOAuthSecurityRestConfig -- Constructor ");
        this.jwtToken = jwtToken;
        if(this.jwtToken != null){
            log.debug("JwtToken: ", jwtToken.toString());
        }
    }


    public Properties getProducerProperties() {
        log.debug("KafkaOAuthSecurityRestConfig -- getProducerProperties ");
        Properties originalProps = super.getProducerProperties();
        if (this.jwtToken != null) {
            originalProps = this.getTokenClientProps(originalProps);
        }
        log.debug("originalProps: " + originalProps.toString());
        return originalProps;
    }


    public Properties getConsumerProperties() {
        log.debug("KafkaOAuthSecurityRestConfig -- getConsumerProperties ");
        Properties originalProps = super.getConsumerProperties();
        if (this.jwtToken != null) {
            log.debug("----> JWT Token not null <----");
            originalProps = getTokenClientProps(originalProps);
        }
        log.debug("originalProps: " + originalProps.toString());
        return originalProps;
    }


    public Properties getAdminProperties() {
        log.debug("KafkaOAuthSecurityRestConfig -- getAdminProperties ");
        Properties originalProps = super.getAdminProperties();
        if (this.jwtToken != null) {
            log.debug("----> JWT Token not null <----");
            originalProps = getTokenClientProps(originalProps);
        }
        log.debug("originalProps: " + originalProps.toString());
        return originalProps;
    }

    public Properties getTokenClientProps(Properties properties){
        log.debug("KafkaOAuthSecurityRestConfig -- getTokenClientProps");
        Properties localProperties = new Properties();
        for(Object key : this.getOriginalProperties().keySet()){
            if (key.toString().startsWith("ims.rest.client.")){
                localProperties.put(key.toString().replace("ims.rest.client.", ""), this.getOriginalProperties().get(key));
            }
        }
        if (this.getOriginalProperties().getProperty("client.sasl.mechanism") != null &&
                this.getOriginalProperties().getProperty("client.sasl.mechanism").equalsIgnoreCase("OAUTHBEARER")) {
            localProperties.put("sasl.jaas.config", "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required  " +
                    "ims.access.token=\"" + this.jwtToken.value() + "\";");
        }
        log.debug("LocalProperties: " + localProperties.toString());
        properties.putAll(localProperties);
        log.debug("Properties: " + properties.toString());
        return properties;
    }

    private static ConfigDef createBaseConfigDef() {
        return baseKafkaRestConfigDef()
                .define(
                        "ims.rest.client.sasl.mechanism",
                        ConfigDef.Type.STRING,
                        (Object)"",
                        ConfigDef.Importance.LOW,
                        "The mechanism that will be used at the client connections to the broker")
                .define(
                        "ims.rest.client.security.protocol",
                        ConfigDef.Type.STRING,
                        (Object)"",
                        ConfigDef.Importance.LOW,
                        "The security protocol that will be used at the client connections to the broker")
                .define(
                        "ims.rest.client.sasl.login.callback.handler.class",
                        ConfigDef.Type.STRING,
                        (Object)"",
                        ConfigDef.Importance.LOW,
                        "The SASL login callback handler class that will be used at the client connections to the broker")
                ;
    }
}
