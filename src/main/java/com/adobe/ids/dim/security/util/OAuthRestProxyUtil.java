package com.adobe.ids.dim.security.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Base64;
import java.util.Map;

public class OAuthRestProxyUtil {

    public static IMSBearerTokenJwt getIMSBearerTokenJwtFromBearer(String accessToken) {
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
            return token;
        }
        return token;
    }
}
