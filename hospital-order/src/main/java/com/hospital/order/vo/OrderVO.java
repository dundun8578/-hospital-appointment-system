package com.hospital.order.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 订单VO
 *
 * @author hospital
 * @date 2024-01-15
 */
@Data
@Schema(description = "订单信息")
public class OrderVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "订单ID")
    private Long orderId;

    @Schema(description = "订单号")
    private String orderNo;

    @Schema(description = "预约ID")
    private Long appointmentId;

    @Schema(description = "用户ID")
    private Long userId;

    @Schema(description = "医生ID")
    private Long doctorId;

    @Schema(description = "订单金额（元）")
    private BigDecimal amount;

    @Schema(description = "支付状态：0-待支付 1-已支付 2-已取消 3-已退款")
    private Integer payStatus;

    @Schema(description = "支付方式：1-微信 2-支付宝 3-医保")
    private Integer payType;

    @Schema(description = "支付时间")
    private LocalDateTime payTime;

    @Schema(description = "订单状态：1-待支付 2-已完成 3-已取消")
    private Integer orderStatus;

    @Schema(description = "创建时间")
    private LocalDateTime createdTime;
}
