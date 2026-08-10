package com.hospital.doctor.config;

/**
 * Redis Key常量类
 *
 * @author hospital
 * @date 2024-01-15
 */
public class RedisKeyConstants {

    /**
     * 排班库存Key：hospital:stock:{scheduleId}
     */
    public static final String SCHEDULE_STOCK_KEY = "hospital:stock:%s";

    /**
     * 医院信息缓存Key：hospital:info:{hospitalId}
     */
    public static final String HOSPITAL_INFO_KEY = "hospital:info:%s";

    /**
     * 科室列表缓存Key：hospital:dept:{hospitalId}
     */
    public static final String DEPARTMENT_LIST_KEY = "hospital:dept:%s";

    /**
     * 医生详情缓存Key：doctor:info:{doctorId}
     */
    public static final String DOCTOR_INFO_KEY = "doctor:info:%s";

    /**
     * 医生排班缓存Key：doctor:schedule:{doctorId}:{date}
     */
    public static final String DOCTOR_SCHEDULE_KEY = "doctor:schedule:%s:%s";

    /**
     * 缓存过期时间：24小时
     */
    public static final long CACHE_EXPIRE_HOURS = 24;

    /**
     * 缓存过期时间：1小时
     */
    public static final long CACHE_EXPIRE_1HOUR = 1;

    /**
     * 缓存过期时间：30分钟
     */
    public static final long CACHE_EXPIRE_30MINUTES = 30;
}
