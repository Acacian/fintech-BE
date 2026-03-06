package com.fastcampus.payment.common.idem;

import com.fastcampus.payment.service.IdempotencyService;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Aspect
@Component
public class IdempotencyAspect {

    private static final Logger logger = LoggerFactory.getLogger(IdempotencyAspect.class);

    @Autowired
    private IdempotencyService idempotencyService;

    // @Around 에다가 어떤 method 들을 대상으로 aspect 를 적용할지 지정할 수 있음. 특정 annotation 이 붙은 method 들, 또는 이름이 어떤 패턴인 method 들 등등
    @Around("@annotation(com.fastcampus.payment.common.idem.Idempotent)")
    public Object aspectIdempotency(ProceedingJoinPoint joinPoint) throws Throwable {
        logger.info("IdempotencyAspect start: {}", joinPoint.getSignature().getName());

        String idemKey = extractIdemKey(joinPoint);

        if (idemKey == null || idemKey.isBlank()) {
            logger.debug("Idempotency key missing, proceeding without cache: {}", joinPoint.getSignature().getName());
            return joinPoint.proceed();
        }

        // 이미 동일한 요청이 처리된 경우 기존 결과 반환
        Optional<IdempotencyDto> idempotencyOptional = idempotencyService.checkIdempotency(idemKey);
        if (idempotencyOptional.isPresent()) {
            logger.info("Idempotency cache hit: {}", joinPoint.getSignature().getName());
            return idempotencyOptional.get().getResponseData();
        }

        logger.info("Idempotency cache miss: {}", joinPoint.getSignature().getName());

        // 최초 요청인 경우 target method 를 그대로 진행하고 response data 를 db에 저장
        Object result = joinPoint.proceed();
        IdempotencyDto idempotencyDto = new IdempotencyDto(null, idemKey, result);
        idempotencyService.saveIdempotency(idempotencyDto);
        return result;
    }

    private String extractIdemKey(ProceedingJoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        String idempotencyKey = null;
        for (Object arg : args) {
            if (arg instanceof IdempotencyDto) {
                idempotencyKey = ((IdempotencyDto) arg).getIdempotencyKey();
            }
        }
        return idempotencyKey;
    }
}
