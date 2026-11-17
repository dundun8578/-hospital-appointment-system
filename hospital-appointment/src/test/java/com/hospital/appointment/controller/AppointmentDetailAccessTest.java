package com.hospital.appointment.controller;

import com.hospital.appointment.entity.Appointment;
import com.hospital.appointment.mapper.AppointmentMapper;
import com.hospital.appointment.service.impl.AppointmentServiceImpl;
import com.hospital.common.BusinessException;
import com.hospital.common.GlobalExceptionHandler;
import com.hospital.common.ResultCode;
import com.hospital.common.jwt.JwtInterceptor;
import com.hospital.common.jwt.JwtUtil;
import com.hospital.common.jwt.UserContextHolder;
import com.hospital.doctor.service.DepartmentService;
import com.hospital.doctor.service.DoctorService;
import com.hospital.doctor.service.HospitalService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

/** Exercises real JWT authentication, controller binding and service authorization without middleware. */
class AppointmentDetailAccessTest {

    private static final Long APPOINTMENT_ID = 10L;
    private static final Long OWNER_ID = 101L;
    private static final Long OTHER_USER_ID = 202L;

    private AppointmentMapper appointmentMapper;
    private DoctorService doctorService;
    private HospitalService hospitalService;
    private DepartmentService departmentService;
    private AppointmentServiceImpl appointmentService;
    private JwtUtil jwtUtil;
    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        appointmentMapper = mock(AppointmentMapper.class);
        doctorService = mock(DoctorService.class);
        hospitalService = mock(HospitalService.class);
        departmentService = mock(DepartmentService.class);
        appointmentService = new AppointmentServiceImpl();
        ReflectionTestUtils.setField(appointmentService, "baseMapper", appointmentMapper);
        ReflectionTestUtils.setField(appointmentService, "doctorService", doctorService);
        ReflectionTestUtils.setField(appointmentService, "hospitalService", hospitalService);
        ReflectionTestUtils.setField(appointmentService, "departmentService", departmentService);

        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", "appointment-detail-test-key-not-for-production-0123456789abcdef0123456789abcdef");
        ReflectionTestUtils.setField(jwtUtil, "expiration", 60000L);
        JwtInterceptor jwtInterceptor = new JwtInterceptor();
        ReflectionTestUtils.setField(jwtInterceptor, "jwtUtil", jwtUtil);

        AppointmentController controller = new AppointmentController();
        ReflectionTestUtils.setField(controller, "appointmentService", appointmentService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .addInterceptors(jwtInterceptor)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @AfterEach
    void clearUserContext() {
        UserContextHolder.clear();
    }

    @Test
    void ownerCanReadAppointmentDetail() throws Exception {
        givenAppointment();

        mockMvc.perform(get("/appointment/{id}", APPOINTMENT_ID)
                        .header("Authorization", bearerToken(OWNER_ID)))
                .andExpect(jsonPath("$.code").value(ResultCode.SUCCESS.getCode()))
                .andExpect(jsonPath("$.data.appointmentId").value(APPOINTMENT_ID.intValue()))
                .andExpect(jsonPath("$.data.patientName").value("Test Patient"))
                .andExpect(jsonPath("$.data.patientPhone").value("13800000000"));

        assertNull(UserContextHolder.getUserIdOrNull());
    }

    @Test
    void anotherUserCannotReadPatientDetails() throws Exception {
        givenAppointment();

        mockMvc.perform(get("/appointment/{id}", APPOINTMENT_ID)
                        .header("Authorization", bearerToken(OTHER_USER_ID)))
                .andExpect(jsonPath("$.code").value(ResultCode.APPOINTMENT_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.data").doesNotExist());

        verifyNoInteractions(doctorService, hospitalService, departmentService);
        assertNull(UserContextHolder.getUserIdOrNull());
    }

    @Test
    void queryParameterCannotOverrideAuthenticatedUser() throws Exception {
        givenAppointment();

        mockMvc.perform(get("/appointment/{id}", APPOINTMENT_ID)
                        .header("Authorization", bearerToken(OTHER_USER_ID))
                        .param("userId", OWNER_ID.toString()))
                .andExpect(jsonPath("$.code").value(ResultCode.APPOINTMENT_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.data").doesNotExist());

        verifyNoInteractions(doctorService, hospitalService, departmentService);
    }

    @Test
    void missingAppointmentHasSameResponseAsForeignAppointment() throws Exception {
        mockMvc.perform(get("/appointment/{id}", APPOINTMENT_ID)
                        .header("Authorization", bearerToken(OWNER_ID)))
                .andExpect(jsonPath("$.code").value(ResultCode.APPOINTMENT_NOT_FOUND.getCode()))
                .andExpect(jsonPath("$.message").value(ResultCode.APPOINTMENT_NOT_FOUND.getMessage()))
                .andExpect(jsonPath("$.data").doesNotExist());

        verifyNoInteractions(doctorService, hospitalService, departmentService);
    }

    @Test
    void anonymousRequestCannotReachAppointmentData() throws Exception {
        mockMvc.perform(get("/appointment/{id}", APPOINTMENT_ID))
                .andExpect(jsonPath("$.code").value(ResultCode.UNAUTHORIZED.getCode()))
                .andExpect(jsonPath("$.data").doesNotExist());

        verifyNoInteractions(appointmentMapper, doctorService, hospitalService, departmentService);
    }

    @Test
    void serviceRejectsMissingUserEvenOutsideHttpRequest() {
        BusinessException exception = assertThrows(BusinessException.class,
                () -> appointmentService.getAppointmentDetail(APPOINTMENT_ID, null));

        assertEquals(ResultCode.UNAUTHORIZED.getCode(), exception.getCode());
        verifyNoInteractions(appointmentMapper, doctorService, hospitalService, departmentService);
    }

    private String bearerToken(Long userId) {
        return "Bearer " + jwtUtil.generateToken(userId, "user-" + userId);
    }

    private void givenAppointment() {
        Appointment appointment = new Appointment();
        appointment.setAppointmentId(APPOINTMENT_ID);
        appointment.setUserId(OWNER_ID);
        appointment.setDoctorId(1L);
        appointment.setHospitalId(2L);
        appointment.setDepartmentId(3L);
        appointment.setPatientName("Test Patient");
        appointment.setPatientPhone("13800000000");
        appointment.setTimeSlot(1);
        appointment.setStatus(1);
        when(appointmentMapper.selectById(APPOINTMENT_ID)).thenReturn(appointment);
    }
}
