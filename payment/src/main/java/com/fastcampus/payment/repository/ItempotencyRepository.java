package com.fastcampus.payment.repository;


import com.fastcampus.payment.entity.Idempotency;

import java.util.Optional;

public interface ItempotencyRepository {
    Idempotency save(Idempotency idempotency);

    Optional<Idempotency> findByIdempotencyKey(String idemKey);
}
