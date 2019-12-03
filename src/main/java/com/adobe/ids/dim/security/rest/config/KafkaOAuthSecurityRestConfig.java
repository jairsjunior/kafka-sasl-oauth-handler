package com.adobe.ids.dim.security.rest.config;

import com.adobe.ids.dim.security.util.IMSBearerTokenJwt;
import io.confluent.kafkarest.KafkaRestConfig;
import io.confluent.rest.RestConfigException;
import io.confluent.kafkarest.SystemTime;
import org.apache.kafka.common.config.ConfigDef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;

public final class KafkaOAuthSecurityRestConfig extends KafkaRestConfig {

    private static final Logger log = LoggerFactory.getLogger(KafkaOAuthSecurityRestConfig.class);
    private static final ConfigDef configDef = createBaseConfigDef();

    private IMSBearerTokenJwt jwtToken;
    private String clientBootstrapServers;

    public KafkaOAuthSecurityRestConfig(final Properties props, final IMSBearerTokenJwt jwtToken) throws RestConfigException {
        super(KafkaOAuthSecurityRestConfig.configDef, props, new SystemTime());
        log.info("KafkaOAuthSecurityRestConfig -- Constructor ");
        this.jwtToken = jwtToken;
        for(Object s : props.keySet()){
            log.info("Propertie: " + s + " -- " + props.get(s));
        }
        this.clientBootstrapServers = props.getProperty("ims.rest.client.bootstrap.servers");
        log.info("Client Bootstrap Server: " + this.clientBootstrapServers);
        if(this.jwtToken != null){
            log.info("JwtToken: ", jwtToken.toString());
        }
    }

    public Properties getProducerProperties() {
        log.info("KafkaOAuthSecurityRestConfig -- getProducerProperties ");
        final Properties originalProps = super.getProducerProperties();
        Properties secureProps = new Properties();
        if (this.jwtToken != null) {
            secureProps = this.getTokenClientProps();
        }
        originalProps.putAll(secureProps);
        log.info("originalProps: ", originalProps.toString());
        return originalProps;
    }

    public Properties getConsumerProperties() {
        log.info("KafkaOAuthSecurityRestConfig -- getConsumerProperties ");
        final Properties originalProps = super.getConsumerProperties();
        Properties secureProps = new Properties();
        if (this.jwtToken != null) {
            log.info("----> JWT Token not null <----");
            secureProps = this.getTokenClientProps();
        }
        originalProps.putAll(secureProps);
        return originalProps;
    }

    public Properties getTokenClientProps(){
        log.info("KafkaOAuthSecurityRestConfig -- getTokenClientProps");
        final Properties properties = new Properties();
        if(!this.clientBootstrapServers.isEmpty()){
            log.info("Client Bootstrap Server is not empty");
            properties.put("bootstrap.servers", this.clientBootstrapServers);
        }
        properties.put("sasl.mechanism", "OAUTHBEARER");
        properties.put("security.protocol", "SASL_PLAINTEXT");
        properties.put("sasl.login.callback.handler.class", "com.adobe.ids.dim.security.rest.IMSAuthenticateRestCallbackHandler");
        properties.put("sasl.jaas.config", "org.apache.kafka.common.security.oauthbearer.OAuthBearerLoginModule required  " +
                "ims.access.token=\"" + this.jwtToken.value() + "\";");
        return properties;
    }


    //TODO: Other stuff to see later.. needs organization
    private static ConfigDef createBaseConfigDef() {
        return baseKafkaRestConfigDef()
                .define(
                        "ims.rest.client.bootstrap.servers",
                        ConfigDef.Type.STRING,
                        (Object)"",
                        ConfigDef.Importance.LOW,
                        "Comma separeted list of bootstrap servers and ports that have OAUTHBEARER SASL_PLAINTEXT configured"
                );
    }


    public static final String[] getMountValidNames() {
        ArrayList<String> validNames = new ArrayList<String>();
        validNames.add("OAuth");
        validNames.add("");
        return validNames.toArray(new String[0]);
    }
}
