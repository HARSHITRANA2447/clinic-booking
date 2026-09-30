package com.clinicbooking.clinicbackend.repository;

import com.clinicbooking.clinicbackend.entity.Clinic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface ClinicRepository extends JpaRepository<Clinic, Long> {

    // Counts every non-blocked slot in the window, and how many of them are booked
    @Query("""
        select count(s) as total,
               coalesce(sum(case when s.status = com.clinicbooking.clinicbackend.entity.Slot.Status.BOOKED
                                 then 1 else 0 end), 0) as booked
        from Slot s
        where s.doctor.clinic.id = :clinicId
          and s.startTime >= :from and s.startTime < :to
          and s.status <> com.clinicbooking.clinicbackend.entity.Slot.Status.BLOCKED
        """)
    CrowdCounts countSlots(@Param("clinicId") Long clinicId,
                           @Param("from") LocalDateTime from,
                           @Param("to") LocalDateTime to);
}