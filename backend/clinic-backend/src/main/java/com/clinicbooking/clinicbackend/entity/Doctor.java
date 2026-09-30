package com.clinicbooking.clinicbackend.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "doctors")
@Getter @Setter @NoArgsConstructor
public class Doctor {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "clinic_id")
    private Clinic clinic;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 80)
    private String specialization;

    @Column(length = 150)
    private String qualification;

    @Column(name = "consultation_fee", precision = 8, scale = 2)
    private BigDecimal consultationFee;

    @Column(nullable = false)
    private boolean active = true;
}
