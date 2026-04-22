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

    /*
    public JwtTokenProvider(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }
    */


    //로그인 아이디를 넣어서 JWT 문자열을 만듭니다.
    public String createToken(String loginId) {
        long now = Instant.now().getEpochSecond(); // Instant.now() 현재 시각가져오기 ex)  2026-04-20T12:34:56Z , getEpochSecond() 초단위로 바꿈 1713600000

        // “키-값 쌍을 담는 Map 객체를 만들고, 거기에 내용을 추가, 고정 값이라서 불변객체
        Map<String, Object> header = Map.of(
                "alg", "HS256",
                "typ", "JWT"
        );

        // 바뀔수있는 값들이여서 가변객체
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("sub", loginId);
        payload.put("iat", now); // 발급시간
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

    //토큰이 유효하면 안에 들어있는 loginId를 꺼냅니다.
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

    /**토큰을 구조 검사 → 서명 검증 → 만료(exp) 검증 순서로 확인해 모두 통과하면 payload를 반환**/
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


    /** Map → JSON → Base64(URL-safe, padding 없음) 문자열로 변환**/
    // 데이터를 안전하게 전달하기해 base64 사용
    private String encodeJson(Map<String, Object> value) throws Exception {
        byte[] jsonBytes = objectMapper.writeValueAsBytes(value);
        return Base64.getUrlEncoder().withoutPadding().encodeToString(jsonBytes);
    }

    /**header.payload를 secret으로 HmacSHA256 서명후 base64인코딩**/
    // 서명 값을 만들때 입력값을 바이트로 변환해야함, 알고리즘이 바이트 단위로 작동해서
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
