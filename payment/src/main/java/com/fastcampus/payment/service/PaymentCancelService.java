package com.fastcampus.payment.service;

import com.fastcampus.payment.common.exception.base.HttpException;
import com.fastcampus.payment.common.exception.error.PaymentErrorCode;
import com.fastcampus.payment.common.idem.Idempotent;
import com.fastcampus.payment.common.util.TokenHandler;
import com.fastcampus.payment.entity.Payment;
import com.fastcampus.payment.entity.PaymentStatus;
import com.fastcampus.payment.entity.Transaction;
import com.fastcampus.payment.repository.PaymentRepository;
import com.fastcampus.payment.repository.TransactionRepository;
import com.fastcampus.paymentmethod.entity.CardInfo;
import com.fastcampus.paymentmethod.entity.PaymentMethod;
import com.fastcampus.paymentmethod.entity.PaymentMethodType;
import com.fastcampus.paymentmethod.entity.UseYn;
import com.fastcampus.paymentmethod.repository.CardInfoRepository;
import io.jsonwebtoken.ExpiredJwtException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;


@Service
@RequiredArgsConstructor
public class PaymentCancelService {

    private static final Logger logger = LoggerFactory.getLogger(PaymentCancelService.class);

    private final PaymentRepository paymentRepository;
    private final TransactionRepository transactionRepository;
    private final CardInfoRepository cardInfoRepository;
    private final TokenHandler tokenHandler;

    @Idempotent
    @Transactional
    public Payment cancelPayment(String token, Long merchantId) {
        // payment 조회
        Payment payment = extractPaymentByToken(token);
        validateMerchantAccess(payment, merchantId);
        // 결제 정보가 취소 가능한 status인지 확인
        try {
            // try catch 문을 여기다 두는 게 맞나...?
            checkPaymentStatusCancel(payment);
        } catch(HttpException e) {
            if(PaymentErrorCode.PAYMENT_ALREADY_CANCELED.equals(e.getErrorCode())) {
                return payment;
            } else {
                throw e;
            }
        }
        // 결제 수단이 취소 가능한지 확인
        checkPaymentMethodCancellable(payment);
        // 취소 실행
        doCancelPayment(payment);
        // transaction 생성
        Transaction newTransaction = makeCancelTransaction(payment);
        // payment update
        Payment newPayment = updatePaymentData(payment, newTransaction);

        return newPayment;
    }

    private Payment extractPaymentByToken(String token) {
        // 토큰으로 payment 조회
        Optional<Payment> paymentOpt = Optional.empty();
        try {
            // QR 토큰에서 거래 ID 디코딩
            Long paymentId = tokenHandler.decodeQrToken(token);
            // 거래 조회
            paymentOpt = paymentRepository.findById(paymentId);
        } catch (ExpiredJwtException e) {
            // tokenHandler.decodeQrToken(); 중에 token 만료 exception 발생 시
            throw new HttpException(PaymentErrorCode.PAYMENT_EXPIRED);
        } catch (Exception e) {
            throw new HttpException(PaymentErrorCode.INVALID_PAYMENT_REQUEST, e);
        }
        // payment 있으면 반환하고 없으면 NOT FOUND 예외 발생
        Payment payment = paymentOpt.orElseThrow(() -> new HttpException(PaymentErrorCode.PAYMENT_NOT_FOUND));
        return payment;
    }

    private void validateMerchantAccess(Payment payment, Long merchantId) {
        if (!payment.getMerchantId().equals(merchantId)) {
            throw new HttpException(PaymentErrorCode.FORBIDDEN_REQUEST);
        }
    }

    private void checkPaymentStatusCancel(Payment payment) {
        // 멱등한 api 를 위해 이미 취소된 거래라면 같은 결과 던지기
        if(PaymentStatus.CANCELED.equals(payment.getStatus())) {
            throw new HttpException(PaymentErrorCode.PAYMENT_ALREADY_CANCELED);
        }
        // 결제가 취소 가능한 결제인지 결제 상태 확인
        if(!(PaymentStatus.COMPLETED.equals(payment.getStatus()))) {
            throw new HttpException(PaymentErrorCode.PAYMENT_ILLEGAL_STATE);
        }

    }

    private void checkPaymentMethodCancellable(Payment payment) {
        Transaction transaction = payment.getLastTransaction();
        PaymentMethod paymentMethod = transaction.getPaymentMethod();
        if (UseYn.N.equals(paymentMethod.getUseYn())) {
            throw new HttpException(PaymentErrorCode.INACTIVE_PAYMENT_METHOD);
        }

        if (requiresRegisteredInstrument(paymentMethod.getType())) {
            cardInfoRepository.findByPaymentMethodId(paymentMethod.getId())
                    .orElseThrow(() -> new HttpException(PaymentErrorCode.CARD_NOT_FOUND));
        }
    }

    private boolean requiresRegisteredInstrument(PaymentMethodType paymentMethodType) {
        return switch (paymentMethodType) {
            case CARD, APPLE_PAY, GOOGLE_PAY, BANK_TRANSFER, MOBILE_PAY, CRYPTO, PAYPAL -> true;
        };
    }

    private void doCancelPayment(Payment payment) {
        Transaction transaction = payment.getLastTransaction();
        PaymentMethod paymentMethod = transaction.getPaymentMethod();
        if (requiresRegisteredInstrument(paymentMethod.getType())) {
            cardInfoRepository.findByPaymentMethodId(paymentMethod.getId())
                    .orElseThrow(() -> new HttpException(PaymentErrorCode.CARD_NOT_FOUND));
        }
    }

    private Transaction makeCancelTransaction(Payment payment) {
        Transaction oldTransaction = payment.getLastTransaction();
        Transaction newTransaction = new Transaction(payment);
        newTransaction.setStatus(PaymentStatus.CANCELED);
        newTransaction.setAmount(oldTransaction.getAmount());
        newTransaction.setCardToken(oldTransaction.getCardToken());
        transactionRepository.save(newTransaction);
        return newTransaction;
    }

    private Payment updatePaymentData(Payment payment, Transaction transaction) {
        payment.changeLastTransaction(transaction);
        payment.setStatus(transaction.getStatus());
        return paymentRepository.save(payment);
    }
}
