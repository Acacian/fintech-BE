package com.fastcampus.paymentmethod.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CardInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long cardId;

    @OneToOne
    @JoinColumn(name = "payment_method_id", nullable = false)
    private PaymentMethod paymentMethod;

    @Column(nullable = false, unique = true, length = 36)
    private String token;

    @Column(nullable = false, length = 19)  // 0000-0000-0000-0000
    private String cardNumber;

    @Column(nullable = false, length = 5)  // MM/YY
    private String expiryDate;

    @Column(nullable = false, length = 6)  // YYMMDD or similar
    private String birthDate;

    @Column(nullable = false, length = 2)
    private String cardPw;

    @Column(nullable = false, length = 4)
    private String cvc;

    @Column(nullable = false, length = 100) // 암호화해서 저장하기 때문에 넉넉하게 잡음
    private String paymentPassword;

    @Column(nullable = false, length = 20)
    private String cardCompany;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    private CardType type; // CREDIT, DEBIT 등

    //추가 필드
    private String issuerBank; // 발급 은행 이름


    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;


    // 카드 식별자(UUID) 발급 메서드 (저장 시 호출)
    public void generateToken() {
        this.token = UUID.randomUUID().toString();
    }

    // 결제 비밀번호 업데이트
    public void updatePaymentPassword(String newPaymentPassword) {
        this.paymentPassword = newPaymentPassword;
    }

    // 등록 시 같이 등록
    public void setPaymentMethod(PaymentMethod paymentMethod) {
        this.paymentMethod = paymentMethod;
    }

    public String getLast4() {
        if (this.cardNumber == null || this.cardNumber.isBlank()) {
            throw new IllegalStateException("카드 번호가 비어 있습니다.");
        }
        String digitsOnly = this.cardNumber.replaceAll("[^0-9]", "");
        if (digitsOnly.length() < 4) {
            throw new IllegalStateException("카드 번호 형식이 올바르지 않습니다.");
        }
        return digitsOnly.substring(digitsOnly.length() - 4);
    }

    public String getMaskedNumber() {
        return "****-****-****-" + getLast4();
    }

}
