-- Only used by the isolated in-memory database in AppointmentStockTransactionTest.
DROP TABLE IF EXISTS message_outbox;
DROP TABLE IF EXISTS appointment;
DROP TABLE IF EXISTS schedule;

CREATE TABLE schedule (
    schedule_id BIGINT PRIMARY KEY,
    doctor_id BIGINT, hospital_id BIGINT, department_id BIGINT,
    work_date DATE, time_slot INT,
    total_count INT, available_count INT, price DECIMAL(10, 2),
    status INT, deleted INT DEFAULT 0,
    created_time TIMESTAMP, updated_time TIMESTAMP
);

CREATE TABLE appointment (
    appointment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT, schedule_id BIGINT,
    doctor_id BIGINT, hospital_id BIGINT, department_id BIGINT,
    work_date DATE, time_slot INT,
    patient_name VARCHAR(50) NOT NULL,
    patient_phone VARCHAR(20), patient_id_card VARCHAR(30), id_card_type INT,
    status INT, cancel_reason VARCHAR(255), visit_status INT,
    deleted INT DEFAULT 0,
    created_time TIMESTAMP, updated_time TIMESTAMP
);

CREATE TABLE message_outbox (
    outbox_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    aggregate_id BIGINT, event_type VARCHAR(50), payload TEXT,
    status INT, retry_count INT, next_retry_time TIMESTAMP,
    last_error VARCHAR(500), created_time TIMESTAMP, updated_time TIMESTAMP,
    UNIQUE (aggregate_id, event_type)
);
