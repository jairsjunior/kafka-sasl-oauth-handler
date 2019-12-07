package com.adobe.ids.dim.security.rest;

import com.adobe.ids.dim.security.rest.config.KafkaOAuthSecurityRestConfig;
import com.adobe.ids.dim.security.rest.context.KafkaOAuthRestContextFactory;
import com.adobe.ids.dim.security.rest.filter.OAuthFilter;
import io.confluent.kafkarest.KafkaRestConfig;
import io.confluent.kafkarest.extension.RestResourceExtension;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.Configurable;

public class KafkaOAuthSecurityRestResourceExtension implements RestResourceExtension {

    private static final Logger log = LoggerFactory.getLogger(KafkaOAuthSecurityRestResourceExtension.class);

    public void register(Configurable<?> config, KafkaRestConfig restConfig) {
        try{
            log.info("KafkaOAuthSecurityRestResourceExtension -- register");
            final KafkaOAuthSecurityRestConfig secureKafkaRestConfig = new KafkaOAuthSecurityRestConfig(restConfig.getOriginalProperties(), null);
            log.info("KafkaOAuthSecurityRestResourceExtension -- registering OAuthfilter");
            config.register((Object)new OAuthFilter(secureKafkaRestConfig));
        }catch (Exception e){
            log.info("KafkaOAuthSecurityRestResourceExtension -- exception: ");
            e.printStackTrace();
        }
    }

    public void clean(){
        KafkaOAuthRestContextFactory.getInstance().clean();
    }

}
