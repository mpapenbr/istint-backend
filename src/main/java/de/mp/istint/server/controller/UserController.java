package de.mp.istint.server.controller;

import java.security.Principal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.keycloak.adapters.springsecurity.token.KeycloakAuthenticationToken;
import org.keycloak.representations.AccessToken;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
public class UserController {
    @Autowired
    private ObjectMapper om;

    // @CrossOrigin(origins = { "*" })
    @GetMapping("/user")
    @ResponseBody
    public ResponseEntity<AccessToken> user(@AuthenticationPrincipal Principal principal) throws JsonProcessingException {
        KeycloakAuthenticationToken keycloakAuthenticationToken = (KeycloakAuthenticationToken) principal;
        AccessToken accessToken = keycloakAuthenticationToken.getAccount().getKeycloakSecurityContext().getToken();
        log.info("accessToken {}", om.writeValueAsString(accessToken));

        return new ResponseEntity(accessToken, HttpStatus.OK);
    }
}