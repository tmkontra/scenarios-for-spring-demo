package io.github.tmkontra.scenarios.demo.controllers;

import io.github.tmkontra.scenarios.demo.domain.User;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Objects;
import java.util.Optional;

@RestController
@RequestMapping("/api")
public class DemoController {
    private final static String ADMIN_TOKEN = "abc123";
    @GetMapping("/user")
    public ResponseEntity<?> getUser(
            @AuthenticationPrincipal UserDetails user
            ) {
        if (Objects.nonNull(user)) {
            if (user.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
                return ResponseEntity.ok(new User("superuser"));
            } else {
                return ResponseEntity.ok(new User("customer"));
            }
        }
        return ResponseEntity.ok(new User("anonymous"));
    }

    @GetMapping(value = "/my-report", produces = "text/plain")
    public ResponseEntity<?> getMyReport() {
        return ResponseEntity.ok("<report value='1'>");
    }
}
