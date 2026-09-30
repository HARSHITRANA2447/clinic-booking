package com.clinicbooking.clinicbackend.service;

import com.clinicbooking.clinicbackend.dto.BookingResponse;
import com.clinicbooking.clinicbackend.entity.Booking;
import com.clinicbooking.clinicbackend.entity.Patient;
import com.clinicbooking.clinicbackend.entity.Slot;
import com.clinicbooking.clinicbackend.exception.NotFoundException;
import com.clinicbooking.clinicbackend.exception.RateLimitException;
import com.clinicbooking.clinicbackend.exception.SlotUnavailableException;
import com.clinicbooking.clinicbackend.repository.BookingRepository;
import com.clinicbooking.clinicbackend.repository.PatientRepository;
import com.clinicbooking.clinicbackend.repository.SlotRepository;
import com.clinicbooking.clinicbackend.security.RateLimiterService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BookingService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final SlotRepository slotRepository;
    private final PatientRepository patientRepository;
    private final BookingRepository bookingRepository;
    private final RateLimiterService rateLimiter;

    @Transactional
    public BookingResponse book(Long slotId, Long patientId) {
        if (!rateLimiter.tryConsume("book:" + patientId, 20, Duration.ofMinutes(10))) {
            throw new RateLimitException("Too many booking attempts. Please wait a moment.");
        }
        // Lock first, validate after: a concurrent request waits here, then sees BOOKED
        Slot slot = slotRepository.findByIdForUpdate(slotId)
                .orElseThrow(() -> new NotFoundException("Slot not found"));

        if (slot.getStatus() != Slot.Status.AVAILABLE) {
            throw new SlotUnavailableException("This slot is no longer available");
        }
        if (slot.getStartTime().isBefore(LocalDateTime.now(IST))) {
            throw new SlotUnavailableException("This slot is in the past");
        }

        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new NotFoundException("Patient not found"));

        slot.setStatus(Slot.Status.BOOKED);

        Booking booking = new Booking();
        booking.setSlot(slot);
        booking.setPatient(patient);
        booking.setStatus(Booking.Status.CONFIRMED);
        bookingRepository.saveAndFlush(booking);

        return toResponse(booking);
    }

    @Transactional(readOnly = true)
    public List<BookingResponse> mine(Long patientId) {
        return bookingRepository.findByPatientWithDetails(patientId).stream()
                .map(this::toResponse)
                .toList();
    }

    private BookingResponse toResponse(Booking b) {
        var slot = b.getSlot();
        var doctor = slot.getDoctor();
        var clinic = doctor.getClinic();
        return new BookingResponse(
                b.getId(), b.getStatus().name(),
                doctor.getName(), doctor.getSpecialization(),
                clinic.getName(), clinic.getAddress(),
                slot.getStartTime(), slot.getEndTime());
    }
}