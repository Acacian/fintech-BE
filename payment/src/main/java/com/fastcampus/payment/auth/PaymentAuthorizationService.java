package com.fastcampus.payment.auth;

import com.fastcampus.payment.common.exception.base.HttpException;
import com.fastcampus.payment.common.exception.error.PaymentErrorCode;
import com.fastcampus.paymentmethod.entity.User;
import com.fastcampus.paymentmethod.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Objects;
import javax.crypto.SecretKey;

@Service
@RequiredArgsConstructor
public class PaymentAuthorizationService {

    @Value("${jwt.secret}")
    private String jwtSecret;

    private final UserRepository userRepository;

    private SecretKey signingKey;

    @PostConstruct
    void init() {
        byte[] keyBytes = Decoders.BASE64.decode(jwtSecret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public Long requireMerchantId(String authorizationHeader) {
        Claims claims = parseClaims(resolveBearerToken(authorizationHeader));
        try {
            return Long.parseLong(claims.getSubject());
        } catch (NumberFormatException e) {
            throw new HttpException(PaymentErrorCode.INVALID_ACCESS_TOKEN, e);
        }
    }

    public Long requireUserId(String authorizationHeader) {
        Claims claims = parseClaims(resolveBearerToken(authorizationHeader));
        String email = claims.getSubject();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new HttpException(PaymentErrorCode.USER_NOT_FOUND));

        if (!"ACTIVE".equals(user.getStatus())) {
            throw new HttpException(PaymentErrorCode.FORBIDDEN_REQUEST);
        }

        return user.getUserId();
    }

    public void ensureMerchantAccess(Long authorizedMerchantId, Long requestedMerchantId) {
        if (!Objects.equals(authorizedMerchantId, requestedMerchantId)) {
            throw new HttpException(PaymentErrorCode.FORBIDDEN_REQUEST);
        }
    }

    private Claims parseClaims(String token) {
        try {
            return Jwts.parser()
                    .verifyWith(signingKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
        } catch (JwtException | IllegalArgumentException e) {
            throw new HttpException(PaymentErrorCode.INVALID_ACCESS_TOKEN, e);
        }
    }

    private String resolveBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new HttpException(PaymentErrorCode.MISSING_ACCESS_TOKEN);
        }

        return authorizationHeader.startsWith("Bearer ")
                ? authorizationHeader.substring(7)
                : authorizationHeader;
    }
}
