package com.clinicbooking.clinicbackend.service;

import com.clinicbooking.clinicbackend.dto.*;
import com.clinicbooking.clinicbackend.entity.Booking;
import com.clinicbooking.clinicbackend.entity.Doctor;
import com.clinicbooking.clinicbackend.entity.Slot;
import com.clinicbooking.clinicbackend.exception.*;
import com.clinicbooking.clinicbackend.repository.BookingRepository;
import com.clinicbooking.clinicbackend.repository.DoctorRepository;
import com.clinicbooking.clinicbackend.repository.SlotRepository;
import com.clinicbooking.clinicbackend.security.AuthUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class ClinicStaffService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final BookingRepository bookingRepository;
    private final DoctorRepository doctorRepository;
    private final SlotRepository slotRepository;

    // ---------- Queue ----------

    @Transactional(readOnly = true)
    public List<QueueItem> queue(AuthUser user, LocalDate date) {
        return bookingRepository.findQueue(clinicOf(user),
                        date.atStartOfDay(), date.plusDays(1).atStartOfDay())
                .stream().map(this::toQueueItem).toList();
    }

    @Transactional
    public QueueItem updateBooking(AuthUser user, Long bookingId, Booking.Status target) {
        Long clinicId = clinicOf(user);
        Booking b = bookingRepository.findDetail(bookingId)
                .orElseThrow(() -> new NotFoundException("Booking not found"));
        // Same 404 as "missing" so staff can't probe other clinics' bookings
        if (!b.getSlot().getDoctor().getClinic().getId().equals(clinicId)) {
            throw new NotFoundException("Booking not found");
        }
        if (target == Booking.Status.CONFIRMED) {
            throw new BadRequestException("Choose COMPLETED, NO_SHOW or CANCELLED");
        }
        if (b.getStatus() != Booking.Status.CONFIRMED) {
            throw new ConflictException("Only confirmed bookings can be updated");
        }
        if (target == Booking.Status.CANCELLED) {
            // Same row lock the booking flow uses, so the two can't interleave
            Slot slot = slotRepository.findByIdForUpdate(b.getSlot().getId())
                    .orElseThrow(() -> new NotFoundException("Slot not found"));
            slot.setStatus(Slot.Status.AVAILABLE);
        }
        b.setStatus(target);
        return toQueueItem(b);
    }

    // ---------- Doctors ----------

    @Transactional(readOnly = true)
    public List<StaffDoctorResponse> doctors(AuthUser user) {
        return doctorRepository.findByClinicIdOrderByName(clinicOf(user))
                .stream().map(this::toDoctor).toList();
    }

    @Transactional
    public StaffDoctorResponse updateDoctor(AuthUser user, Long doctorId, UpdateDoctorRequest req) {
        Doctor d = ownedDoctor(user, doctorId);
        if (req.consultationFee() != null) d.setConsultationFee(req.consultationFee());
        if (req.qualification() != null) d.setQualification(req.qualification().trim());
        if (req.active() != null) d.setActive(req.active());
        return toDoctor(d);
    }

    // ---------- Slots ----------

    @Transactional(readOnly = true)
    public List<StaffSlotResponse> slots(AuthUser user, Long doctorId, LocalDate date) {
        ownedDoctor(user, doctorId);
        return slotRepository.findAllForDay(doctorId, date.atStartOfDay(), date.plusDays(1).atStartOfDay())
                .stream().map(this::toSlot).toList();
    }

    @Transactional
    public GenerateSlotsResponse generate(AuthUser user, Long doctorId, GenerateSlotsRequest req) {
        Doctor doctor = ownedDoctor(user, doctorId);
        LocalDateTime now = LocalDateTime.now(IST);

        if (req.date().isBefore(now.toLocalDate()) || req.date().isAfter(now.toLocalDate().plusDays(60))) {
            throw new BadRequestException("Date must be within the next 60 days");
        }
        if (!req.endTime().isAfter(req.startTime())) {
            throw new BadRequestException("End time must be after start time");
        }

        Set<LocalDateTime> existing = new HashSet<>();
        slotRepository.findAllForDay(doctorId, req.date().atStartOfDay(),
                        req.date().plusDays(1).atStartOfDay())
                .forEach(s -> existing.add(s.getStartTime()));

        List<Slot> toCreate = new ArrayList<>();
        int skipped = 0;
        LocalDateTime cursor = req.date().atTime(req.startTime());
        LocalDateTime end = req.date().atTime(req.endTime());
        while (!cursor.plusMinutes(req.minutes()).isAfter(end)) {
            if (existing.contains(cursor) || !cursor.isAfter(now)) {
                skipped++;
            } else {
                Slot s = new Slot();
                s.setDoctor(doctor);
                s.setStartTime(cursor);
                s.setEndTime(cursor.plusMinutes(req.minutes()));
                toCreate.add(s);
            }
            cursor = cursor.plusMinutes(req.minutes());
        }
        slotRepository.saveAll(toCreate);
        return new GenerateSlotsResponse(toCreate.size(), skipped);
    }

    @Transactional
    public StaffSlotResponse setSlotStatus(AuthUser user, Long slotId, Slot.Status target) {
        if (target == Slot.Status.BOOKED) {
            throw new BadRequestException("Slots can only be set to AVAILABLE or BLOCKED");
        }
        Slot s = slotRepository.findByIdForUpdate(slotId)
                .orElseThrow(() -> new NotFoundException("Slot not found"));
        if (!s.getDoctor().getClinic().getId().equals(clinicOf(user))) {
            throw new NotFoundException("Slot not found");
        }
        if (s.getStatus() == Slot.Status.BOOKED) {
            throw new ConflictException("This slot has a booking. Cancel the booking first.");
        }
        s.setStatus(target);
        return toSlot(s);
    }

    // ---------- helpers ----------

    private Long clinicOf(AuthUser user) {
        if (user.clinicId() == null) throw new ForbiddenException("No clinic is linked to this account");
        return user.clinicId();
    }

    private Doctor ownedDoctor(AuthUser user, Long doctorId) {
        Doctor d = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new NotFoundException("Doctor not found"));
        if (!d.getClinic().getId().equals(clinicOf(user))) {
            throw new NotFoundException("Doctor not found");
        }
        return d;
    }

    private QueueItem toQueueItem(Booking b) {
        var s = b.getSlot();
        var d = s.getDoctor();
        return new QueueItem(b.getId(), b.getStatus().name(), s.getId(),
                s.getStartTime(), s.getEndTime(), d.getId(), d.getName(),
                b.getPatient().getName(), b.getPatient().getPhone());
    }

    private StaffDoctorResponse toDoctor(Doctor d) {
        return new StaffDoctorResponse(d.getId(), d.getName(), d.getSpecialization(),
                d.getQualification(), d.getConsultationFee(), d.isActive());
    }

    private StaffSlotResponse toSlot(Slot s) {
        return new StaffSlotResponse(s.getId(), s.getStartTime(), s.getEndTime(), s.getStatus().name());
    }
}