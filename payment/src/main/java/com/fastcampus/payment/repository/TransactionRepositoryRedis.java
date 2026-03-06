package com.fastcampus.payment.repository;

import com.fastcampus.payment.entity.Transaction;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TransactionRepositoryRedis {

    public void update(Transaction transaction) {
        // 현재 구현은 DB를 기준 저장소로 사용하고, Redis 반영은 선택적 최적화로 둡니다.
    }
}
