package com.fastcampus.backoffice.service;

import com.fastcampus.backoffice.dto.ApiKeyDto;
import com.fastcampus.backoffice.entity.ApiKey;
import com.fastcampus.backoffice.entity.Merchant;
import com.fastcampus.backoffice.repository.ApiKeyRepository;
import com.fastcampus.backoffice.repository.MerchantRepository;
import com.fastcampus.common.exception.code.CommonErrorCode;
import com.fastcampus.common.exception.code.MerchantErrorCode;
import com.fastcampus.common.exception.exception.DuplicateKeyException;
import com.fastcampus.common.exception.exception.ForbiddenException;
import com.fastcampus.common.exception.exception.NotFoundException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.crypto.SecretKey;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ApiKeyService {
    private final ApiKeyRepository apiKeyRepository;
    private final MerchantRepository merchantRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration}")
    private Long jwtExpiration;

    @Transactional
    public ApiKeyDto generateApiKey(Long merchantId) {
        Merchant merchant = merchantRepository.findById(merchantId)
            .orElseThrow(() -> new NotFoundException(MerchantErrorCode.NOT_FOUND));

        if (apiKeyRepository.existsByMerchant_MerchantIdAndActiveTrue(merchantId)) {
            throw DuplicateKeyException.of(MerchantErrorCode.DUPLICATE_API_KEY);
        }

        // JWT 토큰 생성
        SecretKey key = Keys.hmacShaKeyFor(Base64.getDecoder().decode(jwtSecret));
        String token = Jwts.builder()
            .setSubject(merchantId.toString())
            .claim("merchantName", merchant.getName())
            .setIssuedAt(new Date())
            .setExpiration(new Date(System.currentTimeMillis() + jwtExpiration))
            .signWith(key)
            .compact();

        // API 키 저장
        ApiKey apiKey = new ApiKey();
        apiKey.setMerchant(merchant);
        apiKey.setEncryptedKey(token);
        apiKey.setActive(true);
        apiKey.setExpiredAt(LocalDateTime.now().plusYears(1));

        ApiKey savedApiKey = apiKeyRepository.save(apiKey);
        return convertToDto(savedApiKey);
    }

    @Transactional
    public ApiKeyDto reissueApiKey(Long merchantId, String oldKey) {
        // 기존 API 키 찾기
        ApiKey oldApiKey = apiKeyRepository.findByEncryptedKey(oldKey)
            .orElseThrow(() -> new NotFoundException(MerchantErrorCode.KEY_NOT_FOUND));
        // 가맹점 ID 검증
        if (!oldApiKey.getMerchant().getMerchantId().equals(merchantId)) {
            throw new ForbiddenException(CommonErrorCode.FORBIDDEN);
        }

        // 기존 API 키 비활성화
        apiKeyRepository.deactivateActiveKeyByMerchantId(merchantId);

        // 새로운 API 키 발급
        return generateApiKey(merchantId);
    }

    @Transactional(readOnly = true)
    public List<ApiKeyDto> getApiKeys(Long merchantId) {
        return apiKeyRepository.findByMerchant_MerchantId(merchantId).stream()
            .map(this::convertToDto)
            .collect(Collectors.toList());
    }

    @Transactional
    public void deactivateApiKey(String key, Long authorizedMerchantId) {
        ApiKey apiKey = apiKeyRepository.findByEncryptedKey(key)
            .orElseThrow(() -> new NotFoundException(MerchantErrorCode.KEY_NOT_FOUND));
        if (!apiKey.getMerchant().getMerchantId().equals(authorizedMerchantId)) {
            throw new ForbiddenException(CommonErrorCode.FORBIDDEN);
        }
        apiKey.setActive(false);
        apiKeyRepository.save(apiKey);
    }

    private ApiKeyDto convertToDto(ApiKey apiKey) {
        ApiKeyDto dto = new ApiKeyDto();
        dto.setId(apiKey.getKeysId());
        dto.setKey(apiKey.getEncryptedKey());
        dto.setActive(apiKey.getActive());
        dto.setExpiredAt(apiKey.getExpiredAt());
        return dto;
    }
} 
