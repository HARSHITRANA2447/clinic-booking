package com.clinicbooking.clinicbackend;

import com.clinicbooking.clinicbackend.exception.SlotUnavailableException;
import com.clinicbooking.clinicbackend.service.BookingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

import static org.assertj.core.api.Assertions.assertThat;

class BookingConcurrencyTest extends AbstractIntegrationTest {

    @Autowired
    BookingService bookingService;

    @Test
    void onlyOneOfManySimultaneousRequestsGetsTheSlot() throws Exception {
        long slotId = slot(doctor(clinic()));
        int n = 8;

        List<Long> patientIds = new ArrayList<>();
        for (int i = 0; i < n; i++) patientIds.add(patient("980000000" + i));

        ExecutorService pool = Executors.newFixedThreadPool(n);
        CountDownLatch ready = new CountDownLatch(n);
        CountDownLatch go = new CountDownLatch(1);
        List<Future<Object>> futures = new ArrayList<>();

        for (long pid : patientIds) {
            futures.add(pool.submit((Callable<Object>) () -> {
                ready.countDown();
                go.await();                       // all threads start together
                try {
                    return bookingService.book(slotId, pid);
                } catch (Exception e) {
                    return e;
                }
            }));
        }

        ready.await();
        go.countDown();

        List<Object> results = new ArrayList<>();
        for (Future<Object> f : futures) results.add(f.get(30, TimeUnit.SECONDS));
        pool.shutdown();

        long successes = results.stream().filter(r -> !(r instanceof Exception)).count();
        List<Object> failures = results.stream().filter(r -> r instanceof Exception).toList();

        assertThat(successes).isEqualTo(1);
        assertThat(failures).hasSize(n - 1);
        // Losers must fail in one of the expected, clean ways, never an unexpected error
        assertThat(failures).allSatisfy(e -> assertThat(e).isInstanceOfAny(
                SlotUnavailableException.class,
                PessimisticLockingFailureException.class,
                DataIntegrityViolationException.class));

        Integer bookings = jdbc.queryForObject(
                "SELECT count(*) FROM bookings WHERE slot_id = ?", Integer.class, slotId);
        String status = jdbc.queryForObject(
                "SELECT status FROM slots WHERE id = ?", String.class, slotId);
        assertThat(bookings).isEqualTo(1);
        assertThat(status).isEqualTo("BOOKED");
    }
}