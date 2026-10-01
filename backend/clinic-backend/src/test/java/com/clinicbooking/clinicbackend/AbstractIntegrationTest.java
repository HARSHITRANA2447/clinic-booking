package com.clinicbooking.clinicbackend;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDateTime;
import java.time.ZoneId;

@SpringBootTest
public abstract class AbstractIntegrationTest {

    static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    // One container shared by every test class in the run
    static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:16-alpine").withInitScript("test-schema.sql");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry r) {
        r.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        r.add("spring.datasource.username", POSTGRES::getUsername);
        r.add("spring.datasource.password", POSTGRES::getPassword);
        r.add("app.jwt.secret", () -> "test-secret-test-secret-test-secret-1234");
    }

    @Autowired
    protected JdbcTemplate jdbc;

    @BeforeEach
    void cleanDatabase() {
        jdbc.execute("TRUNCATE bookings, slots, otp_codes, users, patients, doctors, clinics RESTART IDENTITY CASCADE");
    }

    // ---------- fixtures (plain SQL, so they commit like real data) ----------

    protected long clinic() {
        return jdbc.queryForObject("""
            INSERT INTO clinics(name, address, city, latitude, longitude)
            VALUES ('Test Clinic', 'Test Address', 'Hyderabad', 17.44, 78.39) RETURNING id
            """, Long.class);
    }

    protected long doctor(long clinicId) {
        return jdbc.queryForObject("""
            INSERT INTO doctors(clinic_id, name, specialization)
            VALUES (?, 'Dr. Test', 'General Physician') RETURNING id
            """, Long.class, clinicId);
    }

    protected long slot(long doctorId) {
        return slot(doctorId, LocalDateTime.now(IST).plusDays(1)
                .withHour(10).withMinute(0).withSecond(0).withNano(0));
    }

    protected long slot(long doctorId, LocalDateTime start) {
        return jdbc.queryForObject("""
            INSERT INTO slots(doctor_id, start_time, end_time)
            VALUES (?, ?, ?) RETURNING id
            """, Long.class, doctorId, start, start.plusMinutes(15));
    }

    protected long patient(String phone) {
        return jdbc.queryForObject("""
            INSERT INTO patients(name, phone, consent_at)
            VALUES ('Patient ' || ?, ?, now()) RETURNING id
            """, Long.class, phone, phone);
    }
}