package com.clinicbooking.clinicbackend.repository;
import com.clinicbooking.clinicbackend.entity.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    @Query(value = """
        SELECT * FROM (
          SELECT d.id AS "doctorId",
                 d.name AS "doctorName",
                 d.specialization AS "specialization",
                 d.consultation_fee AS "consultationFee",
                 c.id AS "clinicId",
                 c.name AS "clinicName",
                 c.address AS "address",
                 6371 * acos(LEAST(1.0,
                     cos(radians(:lat)) * cos(radians(c.latitude))
                       * cos(radians(c.longitude) - radians(:lng))
                     + sin(radians(:lat)) * sin(radians(c.latitude)))) AS "distanceKm"
          FROM doctors d
          JOIN clinics c ON c.id = d.clinic_id
          WHERE d.active = true
            AND (CAST(:spec AS text) IS NULL
                 OR LOWER(d.specialization) = LOWER(CAST(:spec AS text)))
        ) t
        WHERE t."distanceKm" <= :radiusKm
        ORDER BY t."distanceKm"
        """, nativeQuery = true)
    List<DoctorSearchRow> search(@Param("lat") double lat,
                                 @Param("lng") double lng,
                                 @Param("radiusKm") double radiusKm,
                                 @Param("spec") String spec);
    @Query("select d from Doctor d join fetch d.clinic where d.id = :id and d.active = true")
    Optional<Doctor> findDetail(@Param("id") Long id);
    List<Doctor> findByClinicIdOrderByName(Long clinicId);
}
