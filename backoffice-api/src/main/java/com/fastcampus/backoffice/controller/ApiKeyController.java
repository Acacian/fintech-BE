package com.fastcampus.backoffice.controller;

import com.fastcampus.backoffice.dto.ApiKeyDto;
import com.fastcampus.backoffice.service.ApiKeyService;
import com.fastcampus.backoffice.service.MerchantAccessService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/merchants/api-keys")
@RequiredArgsConstructor
@Tag(name = "API Key Management", description = "API Key management endpoints")
public class ApiKeyController {
    private final ApiKeyService apiKeyService;
    private final MerchantAccessService merchantAccessService;

    @PostMapping("/{merchantId}")
    @Operation(summary = "API key 신규 생성")
    public ResponseEntity<ApiKeyDto> generateApiKey(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable Long merchantId
    ) {
        merchantAccessService.ensureMerchantAccess(authorizationHeader, merchantId);
        return ResponseEntity.ok(apiKeyService.generateApiKey(merchantId));
    }

    @PostMapping("/{merchantId}/reissue")
    @Operation(summary = "API key 재발급")
    public ResponseEntity<ApiKeyDto> reissueApiKey(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable("merchantId") Long merchantId,
            @RequestParam("currentKey") String currentKey
    ) {
        merchantAccessService.ensureMerchantAccess(authorizationHeader, merchantId);
        return ResponseEntity.ok(apiKeyService.reissueApiKey(merchantId, currentKey));
    }

    @GetMapping("/{merchantId}")
    @Operation(summary = "가맹점의 API keys조회")
    public ResponseEntity<List<ApiKeyDto>> getApiKeys(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable("merchantId") Long merchantId
    ) {
        merchantAccessService.ensureMerchantAccess(authorizationHeader, merchantId);
        return ResponseEntity.ok(apiKeyService.getApiKeys(merchantId));
    }

    @DeleteMapping("/{key}")
    @Operation(summary = "API key 비활성화")
    public ResponseEntity<Void> deactivateApiKey(
            @Parameter(hidden = true) @RequestHeader("Authorization") String authorizationHeader,
            @PathVariable String key
    ) {
        Long authorizedMerchantId = merchantAccessService.resolveAuthorizedMerchantId(authorizationHeader);
        apiKeyService.deactivateApiKey(key, authorizedMerchantId);
        return ResponseEntity.ok().build();
    }
} 
