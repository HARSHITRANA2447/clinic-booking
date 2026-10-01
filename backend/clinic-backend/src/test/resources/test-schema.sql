CREATE TABLE clinics (
                         id BIGSERIAL PRIMARY KEY,
                         name VARCHAR(150) NOT NULL,
                         address VARCHAR(300) NOT NULL,
                         city VARCHAR(80) NOT NULL,
                         latitude DOUBLE PRECISION NOT NULL,
                         longitude DOUBLE PRECISION NOT NULL,
                         phone VARCHAR(20),
                         created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE doctors (
                         id BIGSERIAL PRIMARY KEY,
                         clinic_id BIGINT NOT NULL REFERENCES clinics(id),
                         name VARCHAR(120) NOT NULL,
                         specialization VARCHAR(80) NOT NULL,
                         qualification VARCHAR(150),
                         consultation_fee NUMERIC(8,2),
                         active BOOLEAN NOT NULL DEFAULT true
);

CREATE TABLE patients (
                          id BIGSERIAL PRIMARY KEY,
                          name VARCHAR(120) NOT NULL,
                          phone VARCHAR(15) NOT NULL UNIQUE,
                          email VARCHAR(150),
                          consent_at TIMESTAMP NOT NULL,
                          created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE slots (
                       id BIGSERIAL PRIMARY KEY,
                       doctor_id BIGINT NOT NULL REFERENCES doctors(id),
                       start_time TIMESTAMP NOT NULL,
                       end_time TIMESTAMP NOT NULL,
                       status VARCHAR(12) NOT NULL DEFAULT 'AVAILABLE'
                           CHECK (status IN ('AVAILABLE','BOOKED','BLOCKED')),
                       UNIQUE (doctor_id, start_time)
);

CREATE TABLE bookings (
                          id BIGSERIAL PRIMARY KEY,
                          slot_id BIGINT NOT NULL REFERENCES slots(id),
                          patient_id BIGINT NOT NULL REFERENCES patients(id),
                          status VARCHAR(12) NOT NULL DEFAULT 'CONFIRMED'
                              CHECK (status IN ('CONFIRMED','CANCELLED','COMPLETED','NO_SHOW')),
                          created_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE UNIQUE INDEX uq_bookings_active_slot
    ON bookings(slot_id)
    WHERE status <> 'CANCELLED';

CREATE TABLE users (
                       id BIGSERIAL PRIMARY KEY,
                       phone VARCHAR(15) NOT NULL UNIQUE,
                       password_hash VARCHAR(100) NOT NULL,
                       role VARCHAR(15) NOT NULL CHECK (role IN ('PATIENT','CLINIC_STAFF','ADMIN')),
                       patient_id BIGINT UNIQUE REFERENCES patients(id),
                       clinic_id BIGINT REFERENCES clinics(id),
                       phone_verified BOOLEAN NOT NULL DEFAULT false,
                       created_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE TABLE otp_codes (
                           id BIGSERIAL PRIMARY KEY,
                           phone VARCHAR(15) NOT NULL,
                           code_hash VARCHAR(100) NOT NULL,
                           purpose VARCHAR(20) NOT NULL,
                           expires_at TIMESTAMP NOT NULL,
                           consumed BOOLEAN NOT NULL DEFAULT false,
                           attempts INT NOT NULL DEFAULT 0,
                           created_at TIMESTAMP NOT NULL DEFAULT now()
);
CREATE INDEX idx_otp_phone ON otp_codes(phone, purpose, consumed);

CREATE INDEX idx_doctors_spec
    ON doctors(specialization);

CREATE INDEX idx_slots_doc_time
    ON slots(doctor_id, start_time);

CREATE INDEX idx_clinics_geo
    ON clinics(latitude, longitude);