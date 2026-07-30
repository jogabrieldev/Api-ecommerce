package com.api.e_commerce.infrastructure.security;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class JwtService {

    private static final Base64.Encoder BASE64_URL_ENCODER =
            Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER =
            Base64.getUrlDecoder();

    private final ObjectMapper objectMapper;
    private final byte[] secret;
    private final long expirationSeconds;

    public JwtService(ObjectMapper objectMapper,
                      @Value("${security.jwt.secret}") String secret,
                      @Value("${security.jwt.expiration-seconds:3600}") long expirationSeconds) {
        this.objectMapper = objectMapper;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
        if (this.secret.length < 32) {
            throw new IllegalArgumentException("security.jwt.secret must contain at least 32 characters");
        }
    }

    public Token generate(String email) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(expirationSeconds);

        Map<String, Object> header = Map.of("alg", "HS256", "typ", "JWT");
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", email);
        payload.put("iat", now.getEpochSecond());
        payload.put("exp", expiresAt.getEpochSecond());

        String unsignedToken = encode(header) + "." + encode(payload);
        String token = unsignedToken + "." + sign(unsignedToken);
        return new Token(token, expirationSeconds);
    }

    public String validateAndGetSubject(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid token");
        }

        String unsignedToken = parts[0] + "." + parts[1];
        byte[] providedSignature = BASE64_URL_DECODER.decode(parts[2]);
        byte[] expectedSignature = hmac(unsignedToken);
        if (!java.security.MessageDigest.isEqual(providedSignature, expectedSignature)) {
            throw new IllegalArgumentException("Invalid token signature");
        }

        try {
            Map<String, Object> claims = objectMapper.readValue(
                    BASE64_URL_DECODER.decode(parts[1]),
                    new TypeReference<>() {
                    });
            String subject = (String) claims.get("sub");
            Number expiration = (Number) claims.get("exp");
            if (subject == null || subject.isBlank() || expiration == null
                    || Instant.now().getEpochSecond() >= expiration.longValue()) {
                throw new IllegalArgumentException("Expired or invalid token");
            }
            return subject;
        } catch (Exception exception) {
            throw new IllegalArgumentException("Invalid token", exception);
        }
    }

    private String encode(Object value) {
        try {
            return BASE64_URL_ENCODER.encodeToString(objectMapper.writeValueAsBytes(value));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not generate token", exception);
        }
    }

    private String sign(String content) {
        return BASE64_URL_ENCODER.encodeToString(hmac(content));
    }

    private byte[] hmac(String content) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret, "HmacSHA256"));
            return mac.doFinal(content.getBytes(StandardCharsets.UTF_8));
        } catch (Exception exception) {
            throw new IllegalStateException("Could not sign token", exception);
        }
    }

    public record Token(String value, long expiresIn) {
    }
}
