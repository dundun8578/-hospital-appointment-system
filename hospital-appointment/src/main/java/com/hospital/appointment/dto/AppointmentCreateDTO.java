package com.hospital.appointment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

/**
 * 预约请求DTO
 *
 * @author hospital
 * @date 2024-01-15
 */
@Data
public class AppointmentCreateDTO {

    /**
     * 排班ID
     */
    @NotNull(message = "排班ID不能为空")
    private Long scheduleId;

    /**
     * 就诊人姓名
     */
    @NotBlank(message = "就诊人姓名不能为空")
    private String patientName;

    /**
     * 就诊人手机号
     */
    @NotBlank(message = "就诊人手机号不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
    private String patientPhone;

    /**
     * 就诊人身份证号
     */
    @NotBlank(message = "就诊人身份证号不能为空")
    @Pattern(regexp = "^[1-9]\\d{5}(18|19|20)\\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\\d|3[01])\\d{3}[\\dXx]$", message = "身份证号格式不正确")
    private String patientIdCard;

    /**
     * 证件类型：1-身份证 2-护照 3-港澳通行证
     */
    private Integer idCardType = 1;
}
