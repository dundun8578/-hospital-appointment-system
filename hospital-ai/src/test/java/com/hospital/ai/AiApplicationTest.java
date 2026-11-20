package com.hospital.ai;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.ai.client.LlmClient;
import com.hospital.ai.config.AiProperties;
import com.hospital.common.utils.IpBlacklistUtil;
import com.hospital.common.utils.RedisRateLimitUtil;
import com.hospital.doctor.controller.DepartmentController;
import com.hospital.doctor.controller.DoctorController;
import com.hospital.doctor.controller.HospitalController;
import com.hospital.doctor.controller.ScheduleController;
import com.hospital.doctor.mapper.DepartmentMapper;
import com.hospital.doctor.service.DepartmentService;
import com.hospital.doctor.task.ScheduleTask;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.redisson.api.RedissonClient;
import org.redisson.spring.data.connection.RedissonConnectionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.ApplicationContext;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.content;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Real application and mapper wiring; external Redis and LLM connections are isolated. */
@SpringBootTest(classes = AiApplication.class, properties = {
        "spring.profiles.active=test",
        "spring.datasource.url=jdbc:h2:mem:ai_startup;MODE=MySQL;DB_CLOSE_DELAY=-1",
        "spring.datasource.driver-class-name=org.h2.Driver",
        "spring.datasource.username=sa",
        "spring.datasource.password=",
        "spring.autoconfigure.exclude=org.springframework.boot.autoconfigure.amqp.RabbitAutoConfiguration",
        "ai.enabled=false",
        "ai.base-url=http://llm.test",
        "ai.api-key=test-only",
        "ai.model=test-model",
        "logging.level.com.hospital=INFO"
})
@AutoConfigureMockMvc
class AiApplicationTest {

    @Autowired
    private ApplicationContext context;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private AiProperties aiProperties;
    @Autowired
    private LlmClient llmClient;
    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RedissonClient redissonClient;
    @MockBean
    private RedissonConnectionFactory redisConnectionFactory;
    @MockBean
    private IpBlacklistUtil ipBlacklistUtil;
    @MockBean
    private RedisRateLimitUtil redisRateLimitUtil;

    private MockRestServiceServer llmServer;

    @BeforeEach
    void setUp() {
        aiProperties.setEnabled(false);
        when(redisRateLimitUtil.tryAcquire(anyString(), anyInt(), anyInt())).thenReturn(true);
        RestTemplate restTemplate = (RestTemplate) ReflectionTestUtils.getField(llmClient, "restTemplate");
        llmServer = MockRestServiceServer.bindTo(restTemplate).build();
        jdbcTemplate.execute("""
                CREATE TABLE IF NOT EXISTS department (
                    department_id BIGINT PRIMARY KEY,
                    hospital_id BIGINT,
                    department_name VARCHAR(50),
                    department_code VARCHAR(50),
                    parent_id BIGINT,
                    description VARCHAR(500),
                    location VARCHAR(100),
                    status TINYINT,
                    deleted TINYINT,
                    created_time TIMESTAMP,
                    updated_time TIMESTAMP
                )
                """);
        jdbcTemplate.update("DELETE FROM department");
        jdbcTemplate.update("""
                INSERT INTO department (department_id, hospital_id, department_name, status, deleted)
                VALUES (7, 1, 'Test Department', 1, 0)
                """);
    }

    @AfterEach
    void verifyExternalRequests() {
        aiProperties.setEnabled(false);
        llmServer.verify();
    }

    @Test
    void startsWithRealDepartmentServiceAndMappersWithoutDoctorEndpointsOrTasks() {
        assertThat(context.getBeansOfType(DepartmentService.class)).hasSize(1);
        DepartmentMapper mapper = context.getBean(DepartmentMapper.class);
        assertThat(mapper.selectById(7L).getDepartmentName()).isEqualTo("Test Department");
        assertThat(context.getBeansOfType(DepartmentController.class)).isEmpty();
        assertThat(context.getBeansOfType(DoctorController.class)).isEmpty();
        assertThat(context.getBeansOfType(HospitalController.class)).isEmpty();
        assertThat(context.getBeansOfType(ScheduleController.class)).isEmpty();
        assertThat(context.getBeansOfType(ScheduleTask.class)).isEmpty();
    }

    @Test
    void disabledAiReturnsFallbackWithoutCallingLlm() throws Exception {
        mockMvc.perform(post("/ai/triage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symptoms\":\"test symptoms\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.fallback").value(true))
                .andExpect(jsonPath("$.data.recommendations").isEmpty());
    }

    @Test
    void enabledAiUsesDepartmentsLoadedThroughRegisteredMappers() throws Exception {
        aiProperties.setEnabled(true);
        String answer = objectMapper.writeValueAsString(Map.of(
                "recommendations", List.of(Map.of(
                        "departmentId", 7,
                        "departmentName", "Test Department",
                        "reason", "test reason")),
                "advice", "test advice"));
        String response = objectMapper.writeValueAsString(Map.of(
                "choices", List.of(Map.of("message", Map.of("content", answer)))));
        llmServer.expect(requestTo("http://llm.test/chat/completions"))
                .andExpect(method(HttpMethod.POST))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Test Department")))
                .andRespond(withSuccess(response, MediaType.APPLICATION_JSON));

        mockMvc.perform(post("/ai/triage")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"symptoms\":\"test symptoms\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.fallback").value(false))
                .andExpect(jsonPath("$.data.recommendations[0].departmentId").value(7))
                .andExpect(jsonPath("$.data.recommendations[0].departmentName").value("Test Department"));
    }
}
