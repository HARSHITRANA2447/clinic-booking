package com.clinicbooking.clinicbackend;

import com.clinicbooking.clinicbackend.entity.Booking;
import com.clinicbooking.clinicbackend.entity.User;
import com.clinicbooking.clinicbackend.exception.NotFoundException;
import com.clinicbooking.clinicbackend.exception.SlotUnavailableException;
import com.clinicbooking.clinicbackend.security.AuthUser;
import com.clinicbooking.clinicbackend.service.BookingService;
import com.clinicbooking.clinicbackend.service.ClinicStaffService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BookingLifecycleTest extends AbstractIntegrationTest {

    @Autowired BookingService bookingService;
    @Autowired ClinicStaffService staffService;

    @Test
    void bookingAnAlreadyBookedSlotFails() {
        long slot = slot(doctor(clinic()));
        long p1 = patient("9800000001");
        long p2 = patient("9800000002");

        bookingService.book(slot, p1);

        assertThatThrownBy(() -> bookingService.book(slot, p2))
                .isInstanceOf(SlotUnavailableException.class);
    }

    @Test
    void pastSlotsCannotBeBooked() {
        long doctor = doctor(clinic());
        long past = slot(doctor, LocalDateTime.now(IST).minusHours(2));
        long p = patient("9800000001");

        assertThatThrownBy(() -> bookingService.book(past, p))
                .isInstanceOf(SlotUnavailableException.class);
    }

    @Test
    void cancelledSlotCanBeBookedAgain() {
        long clinic = clinic();
        long slot = slot(doctor(clinic));
        long p1 = patient("9800000001");
        long p2 = patient("9800000002");
        var staff = new AuthUser(1L, null, clinic, User.Role.CLINIC_STAFF);

        var first = bookingService.book(slot, p1);
        staffService.updateBooking(staff, first.bookingId(), Booking.Status.CANCELLED);
        var second = bookingService.book(slot, p2);   // proves the partial unique index works

        assertThat(second.bookingId()).isNotEqualTo(first.bookingId());
    }

    @Test
    void staffCannotModifyAnotherClinicsBooking() {
        long clinicA = clinic();
        long clinicB = clinic();
        long slot = slot(doctor(clinicA));
        long p = patient("9800000001");
        var booking = bookingService.book(slot, p);
        var staffOfB = new AuthUser(1L, null, clinicB, User.Role.CLINIC_STAFF);

        assertThatThrownBy(() -> staffService.updateBooking(
                staffOfB, booking.bookingId(), Booking.Status.CANCELLED))
                .isInstanceOf(NotFoundException.class);
    }
}