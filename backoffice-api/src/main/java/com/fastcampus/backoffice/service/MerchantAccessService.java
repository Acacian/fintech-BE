package com.fastcampus.backoffice.service;

import com.fastcampus.backoffice.entity.Merchant;
import com.fastcampus.backoffice.repository.MerchantRepository;
import com.fastcampus.common.auth.JwtValidator;
import com.fastcampus.common.exception.code.AuthErrorCode;
import com.fastcampus.common.exception.code.CommonErrorCode;
import com.fastcampus.common.exception.code.MerchantErrorCode;
import com.fastcampus.common.exception.exception.ForbiddenException;
import com.fastcampus.common.exception.exception.NotFoundException;
import com.fastcampus.common.exception.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
@RequiredArgsConstructor
public class MerchantAccessService {

    private final JwtValidator jwtValidator;
    private final MerchantRepository merchantRepository;

    public Long resolveAuthorizedMerchantId(String authorizationHeader) {
        String token = resolveBearerToken(authorizationHeader);
        if (!jwtValidator.validate(token)) {
            throw new UnauthorizedException(CommonErrorCode.UNAUTHORIZED);
        }

        String loginId = jwtValidator.getSubject(token);
        Merchant merchant = merchantRepository.findByLoginId(loginId)
                .orElseThrow(() -> new NotFoundException(MerchantErrorCode.NOT_FOUND));

        if (merchant.getStatus() != Merchant.MerchantStatus.ACTIVE) {
            throw new ForbiddenException(CommonErrorCode.FORBIDDEN);
        }

        return merchant.getMerchantId();
    }

    public void ensureMerchantAccess(String authorizationHeader, Long merchantId) {
        Long authorizedMerchantId = resolveAuthorizedMerchantId(authorizationHeader);
        if (!Objects.equals(authorizedMerchantId, merchantId)) {
            throw new ForbiddenException(CommonErrorCode.FORBIDDEN);
        }
    }

    private String resolveBearerToken(String authorizationHeader) {
        if (authorizationHeader == null || authorizationHeader.isBlank()) {
            throw new UnauthorizedException(AuthErrorCode.MISSING_ACCESS_TOKEN);
        }

        return authorizationHeader.startsWith("Bearer ")
                ? authorizationHeader.substring(7)
                : authorizationHeader;
    }
}
