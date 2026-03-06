package com.fastcampus.payment.common.util;

import com.fastcampus.payment.entity.Payment;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Date;
import javax.crypto.SecretKey;

@Component
@RequiredArgsConstructor
public class TokenHandler {

    @Value("${jwt.secret}")
    private String secret;

    private final CommonUtil commonUtil;

    private SecretKey signingKey;
    private static final String ID_KEY = "paymentId";

    @PostConstruct
    public void init() {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * 거래 ID 기반 JWT QR 토큰 생성
     */
    public String generateTokenWithPayment(Payment payment) {
        long now = System.currentTimeMillis();
        LocalDateTime expTime = payment.getLastTransaction().getExpireAt();
        Date expDate = commonUtil.convertToDate(expTime);
        return Jwts.builder()
                .subject("qr_token")
                .claim(ID_KEY, payment.getId())
                .issuedAt(new Date(now))
                .expiration(expDate)
                .signWith(signingKey)
                .compact();
    }

    /**
     * JWT QR 토큰에서 거래 ID 추출
     */
    public Long decodeQrToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get(ID_KEY, Long.class);
    }

    /**
     * JWT QR 토큰에서 전체 Claims 반환
     */
    public Claims decodeQrTokenToClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
