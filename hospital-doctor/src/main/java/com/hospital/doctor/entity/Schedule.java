package com.hospital.doctor.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 排班实体类
 * 对应数据库表：schedule（核心表，含号源库存）
 *
 * @author hospital
 * @date 2024-01-15
 */
@Data
@TableName("schedule")
public class Schedule implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 排班ID，主键
     */
    @TableId(value = "schedule_id", type = IdType.AUTO)
    private Long scheduleId;

    /**
     * 医生ID
     */
    private Long doctorId;

    /**
     * 医院ID（冗余字段）
     */
    private Long hospitalId;

    /**
     * 科室ID（冗余字段）
     */
    private Long departmentId;

    /**
     * 排班日期
     */
    private LocalDate workDate;

    /**
     * 时段：1-上午 2-下午 3-晚上
     */
    private Integer timeSlot;

    /**
     * 总号源数
     */
    private Integer totalCount;

    /**
     * 剩余号源数（核心字段）
     */
    private Integer availableCount;

    /**
     * 挂号费（元）
     */
    private BigDecimal price;

    /**
     * 状态：0-未发布 1-已发布 2-已约满 3-已停诊
     */
    private Integer status;

    /**
     * 逻辑删除：0-未删除 1-已删除
     */
    @TableLogic
    private Integer deleted;

    /**
     * 创建时间
     */
    @TableField(fill = FieldFill.INSERT)
    private LocalDateTime createdTime;

    /**
     * 更新时间
     */
    @TableField(fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedTime;

    /**
     * 判断是否已约满
     *
     * @return true-已约满，false-未约满
     */
    public boolean isFull() {
        return availableCount != null && availableCount <= 0;
    }

    /**
     * 判断是否已发布
     *
     * @return true-已发布，false-未发布
     */
    public boolean isPublished() {
        return status != null && status == 1;
    }
}
