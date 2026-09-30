package com.clinicbooking.clinicbackend.service;

import com.clinicbooking.clinicbackend.dto.SlotResponse;
import com.clinicbooking.clinicbackend.exception.NotFoundException;
import com.clinicbooking.clinicbackend.repository.DoctorRepository;
import com.clinicbooking.clinicbackend.repository.SlotRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SlotService {

    private static final ZoneId IST = ZoneId.of("Asia/Kolkata");

    private final SlotRepository slotRepository;
    private final DoctorRepository doctorRepository;

    @Transactional(readOnly = true)
    public List<SlotResponse> available(Long doctorId, LocalDate date) {
        if (!doctorRepository.existsById(doctorId)) {
            throw new NotFoundException("Doctor not found");
        }

        LocalDateTime from = date.atStartOfDay();
        LocalDateTime to = date.plusDays(1).atStartOfDay();

        // For today, hide slots that have already started
        LocalDateTime now = LocalDateTime.now(IST);
        if (date.equals(now.toLocalDate()) && now.isAfter(from)) {
            from = now;
        }

        return slotRepository.findAvailable(doctorId, from, to).stream()
                .map(s -> new SlotResponse(s.getId(), s.getStartTime(), s.getEndTime()))
                .toList();
    }
}