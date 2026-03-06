package com.fastcampus.payment.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor   // 🔥 기본 생성자 추가 (테스트에서 setter 사용하려면 필요)
public class PaymentExecutionRequest {

    @NotBlank(message = "paymentToken은 필수입니다.")
    private String paymentToken;

    @NotBlank(message = "cardToken은 필수입니다.")// 🔥 검증 추가
    private String cardToken;

    @NotBlank(message = "paymentMethodType은 필수입니다.")// 🔥 검증 추가
    private String paymentMethodType;  // 🔥 필드 추가!

    private Long userId;  // 🔥 필드 추가!

    @JsonCreator
    public PaymentExecutionRequest(
            @JsonProperty("paymentToken") String paymentToken,
            @JsonProperty("cardToken") String cardToken,
            @JsonProperty("paymentMethodType") String paymentMethodType,
            @JsonProperty("userId") Long userId

    ) {
        this.paymentToken = paymentToken;
        this.cardToken = cardToken;
        this.paymentMethodType = paymentMethodType;
        this.userId = userId;
    }


    // 🔥 검증 메서드 추가
    public void nullCheckRequiredParam() {
        if (paymentToken == null || paymentToken.trim().isEmpty()) {
            throw new IllegalArgumentException("paymentToken은 필수입니다.");
        }
        if (cardToken == null || cardToken.trim().isEmpty()) {
            throw new IllegalArgumentException("cardToken은 필수입니다.");
        }
        if (paymentMethodType == null || paymentMethodType.trim().isEmpty()) {
            throw new IllegalArgumentException("paymentMethodType은 필수입니다.");
        }
        if (userId == null) {
            throw new IllegalArgumentException("userId은 필수입니다.");
        }
    }
}
