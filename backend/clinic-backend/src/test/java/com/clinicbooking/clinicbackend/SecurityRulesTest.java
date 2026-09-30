package com.clinicbooking.clinicbackend;

import com.clinicbooking.clinicbackend.entity.User;
import com.clinicbooking.clinicbackend.security.JwtService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class SecurityRulesTest extends AbstractIntegrationTest {

    @Autowired MockMvc mvc;
    @Autowired JwtService jwtService;

    private String bearer(User.Role role, Long patientId, Long clinicId) {
        User u = new User();
        u.setId(1L);
        u.setRole(role);
        u.setPatientId(patientId);
        u.setClinicId(clinicId);
        return "Bearer " + jwtService.generate(u);
    }

    @Test
    void searchIsPublic() throws Exception {
        mvc.perform(get("/api/search").param("lat", "17.44").param("lng", "78.39"))
                .andExpect(status().isOk());
    }

    @Test
    void bookingWithoutTokenIs401() throws Exception {
        mvc.perform(post("/api/bookings").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"slotId\":1}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void tamperedTokenIs401() throws Exception {
        String token = bearer(User.Role.PATIENT, 1L, null);
        String tampered = token.substring(0, token.length() - 3) + "abc";
        mvc.perform(get("/api/bookings/mine").header("Authorization", tampered))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void patientCannotUseClinicEndpoints() throws Exception {
        mvc.perform(get("/api/clinic/queue")
                        .header("Authorization", bearer(User.Role.PATIENT, 1L, null)))
                .andExpect(status().isForbidden());
    }

    @Test
    void staffCanUseClinicEndpoints() throws Exception {
        mvc.perform(get("/api/clinic/queue")
                        .header("Authorization", bearer(User.Role.CLINIC_STAFF, null, 1L)))
                .andExpect(status().isOk());
    }

    @Test
    void staffCannotUseAdminEndpoints() throws Exception {
        mvc.perform(get("/api/admin/anything")
                        .header("Authorization", bearer(User.Role.CLINIC_STAFF, null, 1L)))
                .andExpect(status().isForbidden());
    }
}