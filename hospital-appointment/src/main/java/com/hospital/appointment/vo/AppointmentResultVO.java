package com.hospital.appointment.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 预约结果VO
 *
 * @author hospital
 * @date 2024-01-15
 */
@Data
@Schema(description = "预约结果")
public class AppointmentResultVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "预约ID")
    private Long appointmentId;

    @Schema(description = "预约成功标识")
    private Boolean success;

    @Schema(description = "提示消息")
    private String message;

    /**
     * 构造函数
     *
     * @param success 是否成功
     * @param message 提示消息
     */
    public AppointmentResultVO(Boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    /**
     * 构造函数
     *
     * @param appointmentId 预约ID
     * @param success       是否成功
     * @param message       提示消息
     */
    public AppointmentResultVO(Long appointmentId, Boolean success, String message) {
        this.appointmentId = appointmentId;
        this.success = success;
        this.message = message;
    }

    /**
     * 成功结果
     *
     * @param appointmentId 预约ID
     * @return 预约结果
     */
    public static AppointmentResultVO success(Long appointmentId) {
        return new AppointmentResultVO(appointmentId, true, "预约成功");
    }

    /**
     * 失败结果
     *
     * @param message 失败消息
     * @return 预约结果
     */
    public static AppointmentResultVO fail(String message) {
        return new AppointmentResultVO(false, message);
    }
}
