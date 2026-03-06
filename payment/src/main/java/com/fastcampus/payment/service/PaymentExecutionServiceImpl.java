package com.fastcampus.payment.service;


import com.fastcampus.payment.common.exception.BadRequestException;
import com.fastcampus.payment.common.exception.error.PaymentErrorCode;
import com.fastcampus.payment.common.util.TokenHandler;
import com.fastcampus.payment.dto.PaymentExecutionResponse;
import com.fastcampus.payment.dto.PaymentExecutionRequest;
import com.fastcampus.payment.entity.*;
import com.fastcampus.payment.repository.*;
import com.fastcampus.paymentmethod.entity.CardInfo;
import com.fastcampus.paymentmethod.entity.PaymentMethod;
import com.fastcampus.paymentmethod.entity.PaymentMethodType;
import com.fastcampus.paymentmethod.entity.UseYn;
import com.fastcampus.paymentmethod.repository.CardInfoRepository;
import com.fastcampus.paymentmethod.repository.PaymentMethodRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.stream.Collectors;


/**
 * 결제 실행 로직을 담당하는 서비스 구현체
 * -거래 token과 카드 token을 받아 승인 여부 판단
 * -거래 상태를 COMPLETED or FAILED 로 변경하고, DB/Redis에 저장
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentExecutionServiceImpl implements PaymentExecutionService {

    private static final DateTimeFormatter CARD_EXPIRY_FORMATTER = DateTimeFormatter.ofPattern("MM/yy");

    private final TransactionRepository transactionRepository;
    private final TransactionRepositoryRedis redisTransactionRepository;
    private final CardInfoRepository cardInfoRepository;
    private final PaymentMethodRepository paymentMethodRepository;
    private final PaymentRepository paymentRepository;
    private final TokenHandler tokenHandler;
    /**
     * 결제 요청을 실행하고 거래 상태를 갱신합니다.
     */
    @Override
    @Transactional
    public PaymentExecutionResponse execute(PaymentExecutionRequest request) {
        log.info("결제 실행 시작 - transactionToken: {}",request.getPaymentToken());
        //입력 값 검증
        validateRequest(request);

        //1. 거래 조회 (Redis -> DB Fallback)
        Payment payment = findPayment(request.getPaymentToken());
        validatePaymentStatus(payment);

        // 3. 카드 & 결제수단 검증
        CardInfo cardInfo = validateAndGetCardInfo(request.getCardToken());
        PaymentMethod paymentMethod = validatePaymentMethod(request.getPaymentMethodType(), request.getUserId(), cardInfo);

        // 4. 결제 방식에 따른 승인 처리
        boolean approvalResult = approvePayment(payment, paymentMethod, cardInfo);

        //상태 결정 및 업데이트(수정)
        PaymentStatus newStatus = approvalResult ? PaymentStatus.COMPLETED : PaymentStatus.FAILED;

        // 데이터 업데이트
        Transaction tx = new Transaction(payment);
        updatePaymentData(payment, tx, paymentMethod, newStatus, request);
        updateTransactionData(payment, tx, paymentMethod, cardInfo);

        //4. DB에 저장
        savePaymentData(payment, tx);

        try {
            redisTransactionRepository.update(tx);
        } catch (Exception e) {
            log.warn("Redis 업데이트 실패, DB는 정상 저장됨", e);
        }


        log.info("결제 실행 완료- paymentId: {}, 상태: {}", payment.getId(), tx.getStatus());

        //6. 결과 반환
        return new PaymentExecutionResponse(payment);
    }

    /**
     * 카드 정보 검증 및 조회
     */
    private CardInfo validateAndGetCardInfo(String cardToken) {
        return cardInfoRepository.findByToken(cardToken)
                .orElseThrow(() -> new BadRequestException(PaymentErrorCode.CARD_NOT_FOUND));
    }

    /**
     * 결제 수단 검증
     */
    private PaymentMethod validatePaymentMethod(String methodType, Long userId, CardInfo cardInfo) {
        // String을 Enum으로 변환하여 검증
        PaymentMethodType enumType;
        try {
            enumType = PaymentMethodType.fromString(methodType);
        } catch (IllegalArgumentException e) {
            throw new BadRequestException(PaymentErrorCode.INVALID_PAYMENT_METHOD);
        }

        List<PaymentMethod> methodList = paymentMethodRepository.findByUserIdAndMethodType(userId, enumType);
        if(methodList.isEmpty()) {
            throw new BadRequestException(PaymentErrorCode.PAYMENT_METHOD_NOT_FOUND);
        }
        List<PaymentMethod> yList = methodList.stream()
                .filter(ele -> UseYn.Y.equals(ele.getUseYn()))
                .collect(Collectors.toList()); 
        if(yList.isEmpty()) {
            throw new BadRequestException(PaymentErrorCode.PAYMENT_METHOD_NOT_FOUND);
        }

        List<PaymentMethod> matchedMethods = yList.stream()
                .filter(method -> method.getId().equals(cardInfo.getPaymentMethod().getId()))
                .collect(Collectors.toList());

        if (matchedMethods.isEmpty()) {
            throw new BadRequestException(PaymentErrorCode.INVALID_PAYMENT_METHOD);
        }
        if (matchedMethods.size() > 1) {
            throw new BadRequestException(PaymentErrorCode.INVALID_PAYMENT_METHOD);
        }

        PaymentMethod method = matchedMethods.get(0);
        if(!method.getId().equals(cardInfo.getPaymentMethod().getId())) {
            throw new BadRequestException(PaymentErrorCode.INVALID_PAYMENT_METHOD);
        }

        return method;
    }

    /**
     * 🔥 수정: PaymentExecutionRequest용 검증 메서드
     */
    private void validateRequest(PaymentExecutionRequest request) {
        if(request.getPaymentToken() == null || request.getPaymentToken().trim().isEmpty()){
            throw new BadRequestException(PaymentErrorCode.PAYMENT_EXECUTION_NULL_VALUE);
        }
        if (request.getCardToken() == null || request.getCardToken().trim().isEmpty()) {
            throw new BadRequestException(PaymentErrorCode.PAYMENT_EXECUTION_NULL_VALUE);
        }
        if (request.getPaymentMethodType() == null || request.getPaymentMethodType().trim().isEmpty()) {
            throw new BadRequestException(PaymentErrorCode.PAYMENT_EXECUTION_NULL_VALUE);
        }
    }

    /**
     * 주어진 거래 토큰으로 Redis에서 거래를 조회하고, 없을 경우 데이터베이스에서 조회합니다.
     *
     * @param token 조회할 결제 정보의 토큰
     * @return 조회된 거래 엔티티
     * @throws RuntimeException 거래를 찾을 수 없는 경우 발생
     */
    private Payment findPayment(String token) {
        Long paymentId = tokenHandler.decodeQrToken(token);

        return paymentRepository.findById(paymentId)
                    .orElseThrow(() -> new BadRequestException(PaymentErrorCode.PAYMENT_NOT_FOUND));
    }

    /**
     * 거래의 상태와 만료 여부를 검증하여 유효하지 않을 경우 예외를 발생시킵니다.
     */
    private void validatePaymentStatus(Payment payment) {
        if (payment.getStatus().isFinal()) {
            throw new BadRequestException(PaymentErrorCode.PAYMENT_ALREADY_PROCESSED);
        }
    }

    private boolean approvePayment(Payment payment, PaymentMethod paymentMethod, CardInfo cardInfo) {
        validatePaymentAmount(payment);
        validateInstrumentState(paymentMethod, cardInfo);
        return true;
    }

    private void validatePaymentAmount(Payment payment) {
        if (payment.getTotalAmount() == null || payment.getTotalAmount() <= 0) {
            throw new BadRequestException(PaymentErrorCode.INVALID_PAYMENT_REQUEST);
        }
    }

    private void validateInstrumentState(PaymentMethod paymentMethod, CardInfo cardInfo) {
        PaymentMethodType methodType = paymentMethod.getType();
        switch (methodType) {
            case CARD, APPLE_PAY, GOOGLE_PAY -> validateCardState(cardInfo);
            case BANK_TRANSFER, MOBILE_PAY, CRYPTO, PAYPAL -> validateRegisteredInstrument(cardInfo);
        }
    }

    private void validateCardState(CardInfo cardInfo) {
        validateRegisteredInstrument(cardInfo);
        if (isExpired(cardInfo.getExpiryDate())) {
            throw new BadRequestException(PaymentErrorCode.CARD_INVALID_STATUS);
        }
    }

    private void validateRegisteredInstrument(CardInfo cardInfo) {
        if (cardInfo.getToken() == null || cardInfo.getToken().isBlank()) {
            throw new BadRequestException(PaymentErrorCode.CARD_NOT_FOUND);
        }
    }

    private boolean isExpired(String expiryDate) {
        try {
            YearMonth yearMonth = YearMonth.parse(expiryDate, CARD_EXPIRY_FORMATTER);
            return yearMonth.atEndOfMonth().isBefore(LocalDate.now());
        } catch (DateTimeParseException e) {
            throw new BadRequestException(PaymentErrorCode.CARD_INVALID_STATUS);
        }
    }

    private void updatePaymentData(Payment payment, Transaction transaction, PaymentMethod paymentMethod, PaymentStatus paymentStatus, PaymentExecutionRequest request) {
        payment.setStatus(paymentStatus);
        payment.changeLastTransaction(transaction);
        payment.setUser(paymentMethod.getUser());
    }

    private void updateTransactionData(Payment payment, Transaction transaction, PaymentMethod paymentMethod, CardInfo cardInfo) {
        transaction.setPaymentMethod(paymentMethod);
        transaction.setCardToken(cardInfo.getToken());
        transaction.changePayment(payment);
        transaction.setStatus(payment.getStatus());
        transaction.setAmount(payment.getTotalAmount());
    }

    private void savePaymentData(Payment payment, Transaction transaction) {
        paymentRepository.save(payment);
        transactionRepository.save(transaction);
    }

}
