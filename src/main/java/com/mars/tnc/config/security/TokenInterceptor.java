/*
 *  TECH-STORE CONFIDENTIAL
 *  Copyright 2024 Tech-Store Corporation All Rights Reserved.
 *  The source code contained or described herein and all documents related to the
 *  source code ("Material") are owned by Tech-Store Malaysia or its suppliers or
 *  licensors. Title to the Material remains with Tech-Store Malaysia or its suppliers
 *  and licensors. The Material contains trade secrets and proprietary and
 *  confidential information of Tech-Store or its suppliers and licensors. The Material
 *  is protected by worldwide copyright and trade secret laws and treaty provisions.
 *  No part of the Material may be used, copied, reproduced, modified, published,
 *  uploaded, posted, transmitted, distributed, or disclosed in any way without
 *  Tech-Store's prior express written permission.
 *
 *  No license under any patent, copyright, trade secret or other intellectual
 *  property right is granted to or conferred upon you by disclosure or delivery of
 *  the Materials, either expressly, by implication, inducement, estoppel or
 *  otherwise. Any license under such intellectual property rights must be express
 *  and approved by Tech-Store in writing.
 */

package com.mars.tnc.config.security;

import com.mars.tnc.constants.RedisDbKeys;
import com.techstore.rds.util.RedisUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

@Component
public class TokenInterceptor implements HandlerInterceptor {
    @Autowired
    private TokenProvider tokenProvider;
    @Autowired
    private RedisUtil redisUtil;

    private static final List<String> WHITELISTED_ENDPOINTS = List.of(
            "/api/v1/internal/[a-zA-Z0-9]+"
    );

    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestUri = request.getRequestURI();
        if (this.isWhitelistedEndpoint(requestUri)) {
            return true;
        }

        String token = getJwtFromRequest(request);
        if (token != null && this.tokenProvider.validateJwtToken(token)) {
            String key = RedisDbKeys.getRedisCacheTokenKey(token);
            boolean isValidToken = this.redisUtil.hasKey(key).booleanValue();
            if (isValidToken) {
                String userLastActKey = RedisDbKeys.getRedisCacheUserLastActivityKey(token);
                this.redisUtil.setCacheObject(userLastActKey, System.currentTimeMillis());
                return true;
            }
            response.sendError(HttpStatus.UNAUTHORIZED.value(), "Token has been invalidated.");
            return false;
        }
        response.sendError(401, "Unauthorized");
        return false;
    }

    private String getJwtFromRequest(HttpServletRequest request) {
        String headerAuth = request.getHeader("Authorization");
        if (StringUtils.hasText(headerAuth) && headerAuth.startsWith("Bearer "))
            return headerAuth.substring(7);
        return null;
    }

    private boolean isWhitelistedEndpoint(String requestUri) {
        return WHITELISTED_ENDPOINTS.stream().anyMatch(requestUri::matches);
    }
}
