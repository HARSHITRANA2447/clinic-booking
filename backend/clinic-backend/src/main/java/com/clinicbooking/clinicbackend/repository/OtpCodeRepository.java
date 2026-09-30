package com.clinicbooking.clinicbackend.repository;

import com.clinicbooking.clinicbackend.entity.OtpCode;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface OtpCodeRepository extends JpaRepository<OtpCode, Long> {
    Optional<OtpCode> findFirstByPhoneAndPurposeAndConsumedFalseOrderByIdDesc(
            String phone, String purpose);
}