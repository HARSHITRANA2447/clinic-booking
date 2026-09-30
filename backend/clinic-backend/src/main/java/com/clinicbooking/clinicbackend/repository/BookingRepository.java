package com.clinicbooking.clinicbackend.repository;

import com.clinicbooking.clinicbackend.entity.Booking;
import com.clinicbooking.clinicbackend.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    // join fetch because open-in-view is off, so lazy loading won't work in controllers
    @Query("""
        select b from Booking b
        join fetch b.slot s
        join fetch s.doctor d
        join fetch d.clinic
        where b.patient.id = :patientId
        order by s.startTime desc
        """)
    List<Booking> findByPatientWithDetails(@Param("patientId") Long patientId);
    @Query("""
        select b from Booking b
        join fetch b.slot s
        join fetch s.doctor d
        join fetch d.clinic
        join fetch b.patient
        where b.id = :id
        """)
    Optional<Booking> findDetail(@Param("id") Long id);

    @Query("""
        select b from Booking b
        join fetch b.slot s
        join fetch s.doctor d
        join fetch b.patient
        where d.clinic.id = :clinicId
          and s.startTime >= :from and s.startTime < :to
        order by s.startTime
        """)
    List<Booking> findQueue(@Param("clinicId") Long clinicId,
                            @Param("from") LocalDateTime from,
                            @Param("to") LocalDateTime to);

}