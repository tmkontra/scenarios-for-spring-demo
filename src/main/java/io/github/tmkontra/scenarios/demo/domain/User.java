package io.github.tmkontra.scenarios.demo.domain;

import com.fasterxml.jackson.annotation.JsonProperty;

public class User {
    @JsonProperty
    private final String role;

    public User(String role) {
        this.role = role;
    }
}
