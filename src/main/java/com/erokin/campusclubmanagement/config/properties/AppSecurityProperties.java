package com.erokin.campusclubmanagement.config.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProperties {

    private final Jwt jwt = new Jwt();

    @Getter
    @Setter
    public static class Jwt {
        /**
         * HMAC secret used for signing tokens. Use at least 32 characters to satisfy HS256 key size.
         */
        private String secret = "change-this-secret-in-production-please-change";

        /**
         * Expiration time for access tokens in milliseconds.
         */
        private long expirationMs = 86_400_000L;
    }
}

