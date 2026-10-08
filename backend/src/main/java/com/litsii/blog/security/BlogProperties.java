package com.litsii.blog.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.List;

@ConfigurationProperties(prefix = "blog")
public record BlogProperties(Admin admin, Cors cors) {

    public record Admin(String username, String passwordHash) {
    }

    public record Cors(List<String> allowedOrigins) {
    }
}
