package com.ing.brokerage.utils;


import com.auth0.jwt.JWT;
import com.auth0.jwt.interfaces.DecodedJWT;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


public class JwtParser {

    public static Map<String, String> jwtParser(){
        Map<String, String> jwtMap = new HashMap<>();
        Jwt jwt = (Jwt) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String keycloakId = jwt.getSubject();

        jwtMap.put("userId", keycloakId);
        Map<String, Object> realmAccess = jwt.getClaimAsMap("realm_access");
        List<String> roles = (List<String>) realmAccess.get("roles");
        String role = roles.stream().filter(x -> x.equalsIgnoreCase("admin")).findFirst().orElse("customer");
        jwtMap.put("role", role);
        return jwtMap;
    }
}
