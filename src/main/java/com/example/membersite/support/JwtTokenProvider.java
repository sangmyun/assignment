package com.example.membersite.support;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    private final ObjectMapper objectMapper;
    @Value("${app.auth.jwt.secret}")
    private String secret;

    @Value("${app.auth.jwt.ttl-seconds:43200}")
    private long ttlSeconds;

    /**
     * Creates a signed JWT for the given login id.
     *
     * @param loginId login id
     * @return compact JWT string
     */
    public String createToken(String loginId) {
        long now = Instant.now().getEpochSecond();

        Map<String, Object> header = Map.of(
                "alg", "HS256",
                "typ", "JWT"
        );

        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", loginId);
        payload.put("iat", now);
        payload.put("exp", now + ttlSeconds);

        try {
            String encodedHeader = encodeJson(header);
            String encodedPayload = encodeJson(payload);
            String signingInput = encodedHeader + "." + encodedPayload;
            String signature = sign(signingInput);
            return signingInput + "." + signature;
        } catch (Exception exception) {
            throw new IllegalStateException("Failed to create JWT token", exception);
        }
    }

    /**
     * Validates token and extracts login id.
     *
     * @param token JWT string
     * @return login id or null when token is invalid
     */
    public String getLoginId(String token) {
        try {
            Map<String, Object> payload = parseAndValidate(token);
            Object subject = payload.get("sub");
            if (!(subject instanceof String loginId) || loginId.isBlank()) {
                return null;
            }
            return loginId;
        } catch (Exception exception) {
            return null;
        }
    }

    /**
     * Parses token and validates structure, signature, and expiration.
     *
     * @param token JWT string
     * @return payload map
     * @throws Exception when token is invalid
     */
    private Map<String, Object> parseAndValidate(String token) throws Exception {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid JWT structure");
        }

        String signingInput = parts[0] + "." + parts[1];
        String expectedSignature = sign(signingInput);
        if (!MessageDigest.isEqual(
                expectedSignature.getBytes(StandardCharsets.US_ASCII),
                parts[2].getBytes(StandardCharsets.US_ASCII)
        )) {
            throw new IllegalArgumentException("Invalid JWT signature");
        }

        byte[] payloadBytes = Base64.getUrlDecoder().decode(parts[1]);
        Map<String, Object> payload = objectMapper.readValue(payloadBytes, new TypeReference<>() {});

        Object expValue = payload.get("exp");
        if (!(expValue instanceof Number expNumber)) {
            throw new IllegalArgumentException("Missing exp claim");
        }

        long now = Instant.now().getEpochSecond();
        if (expNumber.longValue() <= now) {
            throw new IllegalArgumentException("JWT expired");
        }

        return payload;
    }

    /**
     * Encodes a map as base64url JSON without padding.
     *
     * @param value map to encode
     * @return encoded string
     * @throws Exception when serialization fails
     */
    private String encodeJson(Map<String, Object> value) throws Exception {
        byte[] jsonBytes = objectMapper.writeValueAsBytes(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(jsonBytes);
    }

    /**
     * Computes HMAC-SHA256 signature for JWT signing input.
     *
     * @param signingInput token header.payload
     * @return base64url signature
     * @throws GeneralSecurityException when crypto operation fails
     */
    private String sign(String signingInput) throws GeneralSecurityException {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("app.auth.jwt.secret must not be blank");
        }

        Mac mac = Mac.getInstance("HmacSHA256");
        SecretKeySpec keySpec = new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        mac.init(keySpec);
        byte[] signatureBytes = mac.doFinal(signingInput.getBytes(StandardCharsets.UTF_8));
        return Base64.getUrlEncoder().withoutPadding().encodeToString(signatureBytes);
    }
}
