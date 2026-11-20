package com.hospital.appointment.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.extension.spring.MybatisSqlSessionFactoryBean;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hospital.appointment.dto.AppointmentCreateDTO;
import com.hospital.appointment.mapper.AppointmentMapper;
import com.hospital.appointment.mq.AppointmentMessageSender;
import com.hospital.appointment.mq.OrderCancelSender;
import com.hospital.appointment.service.impl.AppointmentMQServiceImpl;
import com.hospital.appointment.service.impl.AppointmentServiceImpl;
import com.hospital.appointment.utils.DistributedLockUtil;
import com.hospital.appointment.utils.RedisStockUtil;
import com.hospital.appointment.vo.AppointmentResultVO;
import com.hospital.common.BusinessException;
import com.hospital.doctor.mapper.ScheduleMapper;
import com.hospital.doctor.service.DepartmentService;
import com.hospital.doctor.service.DoctorService;
import com.hospital.doctor.service.HospitalService;
import com.hospital.doctor.service.impl.ScheduleServiceImpl;
import com.hospital.user.service.UserService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mybatis.spring.annotation.MapperScan;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.jdbc.support.JdbcTransactionManager;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.annotation.EnableTransactionManagement;
import org.springframework.transaction.support.TransactionTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** Real Spring transaction proxies and MyBatis SQL; Redis and RabbitMQ are isolated. */
@SpringJUnitConfig(AppointmentStockTransactionTest.Config.class)
@Sql("/appointment-stock-transaction.sql")
class AppointmentStockTransactionTest {

    private static final Long USER_ID = 10L;
    private static final Long SCHEDULE_ID = 20L;
    private static final int INITIAL_STOCK = 3;

    @Autowired
    private AppointmentMQService appointmentMQService;
    @Autowired
    private AppointmentService appointmentService;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private PlatformTransactionManager transactionManager;

    @MockBean
    private RedisStockUtil redisStockUtil;
    @Autowired
    private DistributedLockUtil distributedLockUtil;
    @MockBean
    private RedissonClient redissonClient;
    @MockBean
    private OrderCancelSender orderCancelSender;
    @MockBean
    private AppointmentMessageSender messageSender;
    @MockBean
    private DoctorService doctorService;
    @MockBean
    private HospitalService hospitalService;
    @MockBean
    private DepartmentService departmentService;
    @MockBean
    private UserService userService;
    @MockBean
    private StringRedisTemplate redisTemplate;

    private AtomicInteger redisStock;
    private final List<Integer> appointmentsAtCompensation = new ArrayList<>();
    private final List<Integer> committedAppointmentsAtUnlock = new ArrayList<>();
    private final List<Integer> redisStockAtUnlock = new ArrayList<>();
    private RLock appointmentLock;
    private ReentrantLock localLock;
    private volatile CountDownLatch competingLockAttempt;

    @BeforeEach
    void setUp() throws InterruptedException {
        jdbcTemplate.update("""
                INSERT INTO schedule (schedule_id, doctor_id, hospital_id, department_id,
                    work_date, time_slot, total_count, available_count, status)
                VALUES (?, 1, 2, 3, ?, 1, ?, ?, 1)
                """, SCHEDULE_ID, LocalDate.now().plusDays(1), INITIAL_STOCK, INITIAL_STOCK);
        redisStock = new AtomicInteger(INITIAL_STOCK);
        // A real local lock models Redisson ownership and contention without an external Redis server.
        localLock = new ReentrantLock();
        appointmentLock = mock(RLock.class);
        when(redissonClient.getLock("appointment:user:10:schedule:20")).thenReturn(appointmentLock);
        when(appointmentLock.tryLock(anyLong(), any(TimeUnit.class)))
                .thenAnswer(invocation -> acquireLocalLock(invocation.getArgument(0), invocation.getArgument(1)));
        when(appointmentLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class)))
                .thenAnswer(invocation -> acquireLocalLock(invocation.getArgument(0), invocation.getArgument(2)));
        when(appointmentLock.isHeldByCurrentThread()).thenAnswer(invocation -> localLock.isHeldByCurrentThread());
        doAnswer(invocation -> {
            // An independent connection observes committed state, not the current transaction's writes.
            try (Connection connection = jdbcTemplate.getDataSource().getConnection();
                 Statement statement = connection.createStatement();
                 ResultSet result = statement.executeQuery("SELECT COUNT(*) FROM appointment")) {
                result.next();
                committedAppointmentsAtUnlock.add(result.getInt(1));
                redisStockAtUnlock.add(redisStock.get());
            } finally {
                localLock.unlock();
            }
            return null;
        }).when(appointmentLock).unlock();
        when(redisStockUtil.getStock(SCHEDULE_ID)).thenAnswer(invocation -> redisStock.get());
        when(redisStockUtil.deductStock(SCHEDULE_ID)).thenAnswer(invocation -> {
            if (redisStock.get() <= 0) {
                return false;
            }
            redisStock.decrementAndGet();
            return true;
        });
        doAnswer(invocation -> {
            appointmentsAtCompensation.add(countAppointments());
            redisStock.incrementAndGet();
            return null;
        }).when(redisStockUtil).restoreStock(SCHEDULE_ID);
    }

    @Test
    void committedBookingKeepsStockDeductedAndPersistsOutbox() {
        AppointmentResultVO result = appointmentMQService.createAppointmentWithMQ(USER_ID, request());

        assertThat(result.getSuccess()).isTrue();
        assertThat(countAppointments()).isEqualTo(1);
        assertThat(countOutbox()).isEqualTo(1);
        assertThat(databaseStock()).isEqualTo(INITIAL_STOCK - 1);
        assertThat(redisStock.get()).isEqualTo(INITIAL_STOCK - 1);
        verify(redisStockUtil, never()).restoreStock(SCHEDULE_ID);
        verifyNoInteractions(messageSender);
        assertThat(committedAppointmentsAtUnlock).containsExactly(1);
        assertLockReleasedOnce();
    }

    @Test
    void outboxInsertFailureRollsBackBookingAndRestoresRedisOnce() {
        rejectOutboxInsert();

        assertThatThrownBy(() -> appointmentMQService.createAppointmentWithMQ(USER_ID, request()))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertRolledBack(INITIAL_STOCK);
    }

    @Test
    void databaseStockConflictRestoresRedisOnlyAfterBookingRollback() {
        jdbcTemplate.update("UPDATE schedule SET available_count = 0 WHERE schedule_id = ?", SCHEDULE_ID);

        assertThatThrownBy(() -> appointmentService.createAppointment(USER_ID, request()))
                .isInstanceOf(BusinessException.class).hasMessageContaining("号源已约满");

        assertRolledBack(0);
    }

    @Test
    void outerRollbackOnlyRestoresStockAfterBothServicesReturn() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            AppointmentResultVO result = appointmentMQService.createAppointmentWithMQ(USER_ID, request());
            assertThat(result.getSuccess()).isTrue();
            assertThat(countAppointments()).isEqualTo(1);
            assertThat(countOutbox()).isEqualTo(1);
            assertThat(redisStock.get()).isEqualTo(INITIAL_STOCK - 1);
            verify(redisStockUtil, never()).restoreStock(SCHEDULE_ID);
            assertThat(localLock.isHeldByCurrentThread()).isTrue();
            status.setRollbackOnly();
        });

        assertRolledBack(INITIAL_STOCK);
    }

    @Test
    void rejectedRedisDeductionDoesNotCreateOrCompensateAnything() {
        redisStock.set(0);

        AppointmentResultVO result = appointmentMQService.createAppointmentWithMQ(USER_ID, request());

        assertThat(result.getSuccess()).isFalse();
        assertThat(countAppointments()).isZero();
        assertThat(countOutbox()).isZero();
        assertThat(databaseStock()).isEqualTo(INITIAL_STOCK);
        assertThat(redisStock.get()).isZero();
        verify(redisStockUtil, never()).restoreStock(SCHEDULE_ID);
        assertLockReleasedOnce();
    }

    @Test
    void compensationFailurePreservesOriginalOutboxErrorAndDatabaseRollback() {
        rejectOutboxInsert();
        doThrow(new IllegalStateException("Redis unavailable"))
                .when(redisStockUtil).restoreStock(SCHEDULE_ID);

        assertThatThrownBy(() -> appointmentMQService.createAppointmentWithMQ(USER_ID, request()))
                .isInstanceOf(DataIntegrityViolationException.class);

        assertThat(countAppointments()).isZero();
        assertThat(countOutbox()).isZero();
        assertThat(databaseStock()).isEqualTo(INITIAL_STOCK);
        assertThat(redisStock.get()).isEqualTo(INITIAL_STOCK - 1);
        verify(redisStockUtil, times(1)).restoreStock(SCHEDULE_ID);
        assertLockReleasedOnce();
    }

    @Test
    void outerCommitKeepsLockUntilTransactionCompletes() {
        new TransactionTemplate(transactionManager).executeWithoutResult(status -> {
            assertThat(appointmentMQService.createAppointmentWithMQ(USER_ID, request()).getSuccess()).isTrue();
            assertThat(localLock.isHeldByCurrentThread()).isTrue();
            verify(appointmentLock, never()).unlock();
        });

        assertThat(committedAppointmentsAtUnlock).containsExactly(1);
        assertLockReleasedOnce();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void competingRequestWaitsForCommitOrRollback(boolean rollbackFirst) throws Exception {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        AtomicReference<Future<AppointmentResultVO>> competing = new AtomicReference<>();
        TransactionTemplate transaction = new TransactionTemplate(transactionManager);
        transaction.setIsolationLevel(TransactionDefinition.ISOLATION_REPEATABLE_READ);
        try {
            transaction.executeWithoutResult(status -> {
                assertThat(appointmentMQService.createAppointmentWithMQ(USER_ID, request()).getSuccess()).isTrue();
                competingLockAttempt = new CountDownLatch(1);
                competing.set(executor.submit(() -> transaction.execute(
                        secondStatus -> appointmentMQService.createAppointmentWithMQ(USER_ID, request()))));
                awaitCompetingAttempt();
                assertThatThrownBy(() -> competing.get().get(150, TimeUnit.MILLISECONDS))
                        .isInstanceOf(TimeoutException.class);
                assertThat(localLock.isHeldByCurrentThread()).isTrue();
                if (rollbackFirst) {
                    status.setRollbackOnly();
                }
            });

            // After commit, reject a duplicate. After rollback, allow the waiting request to retry.
            assertThat(competing.get().get(5, TimeUnit.SECONDS).getSuccess()).isEqualTo(rollbackFirst);
            assertThat(countAppointments()).isEqualTo(1);
            assertThat(countOutbox()).isEqualTo(1);
            assertThat(databaseStock()).isEqualTo(INITIAL_STOCK - 1);
            assertThat(redisStock.get()).isEqualTo(INITIAL_STOCK - 1);
            assertThat(committedAppointmentsAtUnlock).containsExactly(rollbackFirst ? 0 : 1, 1);
            assertThat(redisStockAtUnlock).containsExactly(
                    rollbackFirst ? INITIAL_STOCK : INITIAL_STOCK - 1, INITIAL_STOCK - 1);
            assertThat(localLock.isLocked()).isFalse();
            verify(appointmentLock, times(2)).unlock();
        } finally {
            executor.shutdownNow();
            assertThat(executor.awaitTermination(5, TimeUnit.SECONDS)).isTrue();
        }
    }

    @Test
    void lockAcquisitionUsesWatchdogInsteadOfFixedLease() throws InterruptedException {
        RLock lock = distributedLockUtil.getAppointmentLock(USER_ID, SCHEDULE_ID);
        try {
            assertThat(lock).isSameAs(appointmentLock);
            verify(appointmentLock).tryLock(5, TimeUnit.SECONDS);
            verify(appointmentLock, never()).tryLock(anyLong(), anyLong(), any(TimeUnit.class));
        } finally {
            distributedLockUtil.unlock(lock);
        }
    }

    @Test
    void failedLockAcquisitionDoesNotDeductStockOrUnlock() throws InterruptedException {
        when(appointmentLock.tryLock(anyLong(), any(TimeUnit.class))).thenReturn(false);
        when(appointmentLock.tryLock(anyLong(), anyLong(), any(TimeUnit.class))).thenReturn(false);

        assertThat(appointmentMQService.createAppointmentWithMQ(USER_ID, request()).getSuccess()).isFalse();

        verifyNoInteractions(redisStockUtil);
        verify(appointmentLock, never()).unlock();
        assertThat(countAppointments()).isZero();
    }

    private boolean acquireLocalLock(long waitTime, TimeUnit timeUnit) throws InterruptedException {
        if (competingLockAttempt != null) {
            competingLockAttempt.countDown();
        }
        return localLock.tryLock(waitTime, timeUnit);
    }

    private void awaitCompetingAttempt() {
        try {
            assertThat(competingLockAttempt.await(3, TimeUnit.SECONDS)).isTrue();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new AssertionError("Interrupted while waiting for the competing request", e);
        }
    }

    private void assertLockReleasedOnce() {
        assertThat(localLock.isLocked()).isFalse();
        verify(appointmentLock, times(1)).unlock();
    }

    private void assertRolledBack(int expectedDatabaseStock) {
        assertThat(countAppointments()).isZero();
        assertThat(countOutbox()).isZero();
        assertThat(databaseStock()).isEqualTo(expectedDatabaseStock);
        assertThat(redisStock.get()).isEqualTo(INITIAL_STOCK);
        verify(redisStockUtil, times(1)).restoreStock(SCHEDULE_ID);
        assertThat(appointmentsAtCompensation).containsExactly(0);
        assertThat(committedAppointmentsAtUnlock).containsExactly(0);
        assertThat(redisStockAtUnlock).containsExactly(INITIAL_STOCK);
        assertLockReleasedOnce();
    }

    private void rejectOutboxInsert() {
        jdbcTemplate.execute("""
                ALTER TABLE message_outbox ADD CONSTRAINT reject_appointment_event
                CHECK (event_type <> 'APPOINTMENT_CREATED')
                """);
    }

    private int countAppointments() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM appointment", Integer.class);
    }

    private int countOutbox() {
        return jdbcTemplate.queryForObject("SELECT COUNT(*) FROM message_outbox", Integer.class);
    }

    private int databaseStock() {
        return jdbcTemplate.queryForObject("SELECT available_count FROM schedule WHERE schedule_id = ?",
                Integer.class, SCHEDULE_ID);
    }

    private AppointmentCreateDTO request() {
        AppointmentCreateDTO dto = new AppointmentCreateDTO();
        dto.setScheduleId(SCHEDULE_ID);
        dto.setPatientName("Test Patient");
        dto.setPatientPhone("13800000000");
        dto.setPatientIdCard("test-id-card");
        dto.setIdCardType(1);
        return dto;
    }

    @Configuration(proxyBeanMethods = false)
    @EnableTransactionManagement
    @MapperScan(basePackageClasses = {AppointmentMapper.class, ScheduleMapper.class})
    @Import({AppointmentServiceImpl.class, AppointmentMQServiceImpl.class,
            AppointmentOutboxService.class, ScheduleServiceImpl.class, DistributedLockUtil.class})
    static class Config {
        @Bean
        DataSource dataSource() {
            return new EmbeddedDatabaseBuilder().generateUniqueName(true)
                    .setType(EmbeddedDatabaseType.H2).build();
        }

        @Bean
        JdbcTemplate jdbcTemplate(DataSource dataSource) {
            return new JdbcTemplate(dataSource);
        }

        @Bean
        PlatformTransactionManager transactionManager(DataSource dataSource) {
            return new JdbcTransactionManager(dataSource);
        }

        @Bean
        SqlSessionFactory sqlSessionFactory(DataSource dataSource) throws Exception {
            MybatisSqlSessionFactoryBean factory = new MybatisSqlSessionFactoryBean();
            factory.setDataSource(dataSource);
            MybatisConfiguration configuration = new MybatisConfiguration();
            configuration.setMapUnderscoreToCamelCase(true);
            factory.setConfiguration(configuration);
            return factory.getObject();
        }

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }
    }
}
