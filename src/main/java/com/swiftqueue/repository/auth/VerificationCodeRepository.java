package com.swiftqueue.repository.auth;

import com.swiftqueue.model.auth.VerificationCode;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface VerificationCodeRepository extends JpaRepository<VerificationCode, Long> {

    Optional<VerificationCode> findByCodeAndPhoneNumber(String code, String phoneNumber);

    Optional<VerificationCode> findByPhoneNumber(String number);
}
