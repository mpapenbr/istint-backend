package de.mp.istint.server.controller;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import de.mp.istint.server.config.Redirects;
import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
public class ApiLogin {

    @Autowired
    Redirects validRedirects;

    @CrossOrigin(origins = "*")
    @GetMapping("/app/auth/redirect")
    public void loginWithRedirect(@AuthenticationPrincipal Principal principal, HttpServletResponse response, @RequestParam(value = "redirectUri") String redirectUri) throws IOException {
        log.info("Redirect is {}", redirectUri);
        if (validRedirects.getRedirects().contains(redirectUri)) {
            response.sendRedirect(redirectUri);
            return;
        }
        response.sendError(HttpStatus.FORBIDDEN.value(), "Invalid redirect");
        // TODO: validate redirectUri against configured uri
    }

    // http://localhost:8080/app/auth/redirect?redirectUri=http%3A%2F%2Flocalhost%3A3000

}
