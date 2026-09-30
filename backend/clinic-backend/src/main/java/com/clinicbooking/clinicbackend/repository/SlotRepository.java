package com.clinicbooking.clinicbackend.repository;

import com.clinicbooking.clinicbackend.entity.Slot;
import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface SlotRepository extends JpaRepository<Slot, Long> {

    // Used by BookingService: locks the row until the transaction ends
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @QueryHints(@QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000"))
    @Query("select s from Slot s where s.id = :id")
    Optional<Slot> findByIdForUpdate(@Param("id") Long id);

    // Used by the slot picker
    @Query("""
        select s from Slot s
        where s.doctor.id = :doctorId
          and s.startTime >= :from and s.startTime < :to
          and s.status = com.clinicbooking.clinicbackend.entity.Slot.Status.AVAILABLE
        order by s.startTime
        """)
    List<Slot> findAvailable(@Param("doctorId") Long doctorId,
                             @Param("from") LocalDateTime from,
                             @Param("to") LocalDateTime to);
    @Query("""
        select s from Slot s
        where s.doctor.id = :doctorId
          and s.startTime >= :from and s.startTime < :to
        order by s.startTime
        """)
    List<Slot> findAllForDay(@Param("doctorId") Long doctorId,
                             @Param("from") LocalDateTime from,
                             @Param("to") LocalDateTime to);
}