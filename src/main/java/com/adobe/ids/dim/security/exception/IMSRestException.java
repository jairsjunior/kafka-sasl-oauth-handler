/*
 * ADOBE CONFIDENTIAL. Copyright 2019 Adobe Systems Incorporated. All Rights Reserved. NOTICE: All information contained
 * herein is, and remains the property of Adobe Systems Incorporated and its suppliers, if any. The intellectual and
 * technical concepts contained herein are proprietary to Adobe Systems Incorporated and its suppliers and are protected
 * by all applicable intellectual property laws, including trade secret and copyright law. Dissemination of this
 * information or reproduction of this material is strictly forbidden unless prior written permission is obtained
 * from Adobe Systems Incorporated.
 */

package com.adobe.ids.dim.security.exception;

import io.confluent.rest.exceptions.RestNotAuthorizedException;

public class IMSRestException extends RestNotAuthorizedException {

    public static String BEARER_TOKEN_NOT_SENT_MSG = "Authorization Bearer token not sent";
    public static int BEARER_TOKEN_NOT_SENT_CODE = 40002;
    public static String BEARER_INVALID_TOKEN_MSG = "Invalid Token";
    public static int BEARER_INVALID_TOKEN_CODE = 40003;
    public static String BEARER_SENT_NOT_STARTING_WITH_PREFIX_MSG = "Authorization Bearer sent not starting with ";
    public static int BEARER_SENT_NOT_STARTING_WITH_PREFIX_CODE = 40004;
    public static String BEARER_TOKEN_EXPIRED_MSG = "Bearer token is expired";
    public static int BEARER_TOKEN_EXPIRED_CODE = 40005;
    public static String BEARER_IS_NOT_INSTANCE_IMS_JWT_MSG = "Principal is not a instance of IMSBearerTokenJwt";
    public static int BEARER_IS_NOT_INSTANCE_IMS_JWT_CODE = 40006;

    public IMSRestException(int errCode, String message) {
        super("Error Code: " + String.valueOf(errCode) +", Error Message: " + message, errCode);
    }

}
