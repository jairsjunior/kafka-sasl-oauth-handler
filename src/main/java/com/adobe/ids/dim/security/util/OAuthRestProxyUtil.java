package com.adobe.ids.dim.security.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.confluent.rest.exceptions.RestNotAuthorizedException;
import org.apache.kafka.common.utils.Time;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

public class OAuthRestProxyUtil {

    public static IMSBearerTokenJwt getIMSBearerTokenJwtFromBearer(String accessToken) throws IOException {
        IMSBearerTokenJwt token = null;
        // Get client_id from the token
        String[] tokenString = accessToken.split("\\.");
        Base64.Decoder decoder = Base64.getUrlDecoder();
        String payLoad = new String(decoder.decode(tokenString[1]));
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            Map < String, Object > payloadJson = objectMapper.readValue(payLoad, new TypeReference<Map<String, Object>>(){});
            token = new IMSBearerTokenJwt(payloadJson, accessToken);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RestNotAuthorizedException("Error on decode IMSBearerToken", 40003);
        }
        return token;
    }

    public static boolean validateExpiration(IMSBearerTokenJwt token) {
        return token.lifetimeMs() > Time.SYSTEM.milliseconds();
    }
}
