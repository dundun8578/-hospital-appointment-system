package com.hospital.order.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单实体类
 * 对应数据库表：appointment_order
 *
 * @author hospital
 * @date 2024-01-15
 */
@Data
@TableName("appointment_order")
public class Order implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 订单ID，主键
     */
    @TableId(value = "order_id", type = IdType.AUTO)
    private Long orderId;

    /**
     * 订单号（全局唯一）
     */
    private String orderNo;

    /**
     * 预约ID
     */
    private Long appointmentId;

    /**
     * 用户ID
     */
    private Long userId;

    /**
     * 医生ID（冗余字段）
     */
    private Long doctorId;

    /**
     * 订单金额（元）
     */
    private BigDecimal amount;

    /**
     * 支付状态：0-待支付 1-已支付 2-已取消 3-已退款
     */
    private Integer payStatus;

    /**
     * 支付方式：1-微信 2-支付宝 3-医保
     */
    private Integer payType;

    /**
     * 支付时间
     */
    private LocalDateTime payTime;

    /**
     * 订单状态：1-待支付 2-已完成 3-已取消
     */
    private Integer orderStatus;

    /**
     * 订单备注
     */
    private String remark;

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
}
