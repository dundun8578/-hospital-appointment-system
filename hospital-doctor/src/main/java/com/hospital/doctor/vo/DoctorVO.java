package com.hospital.doctor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 医生VO
 *
 * @author hospital
 * @date 2024-01-15
 */
@Data
@Schema(description = "医生信息")
public class DoctorVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "医生ID")
    private Long doctorId;

    @Schema(description = "医院ID")
    private Long hospitalId;

    @Schema(description = "医院名称")
    private String hospitalName;

    @Schema(description = "科室ID")
    private Long departmentId;

    @Schema(description = "科室名称")
    private String departmentName;

    @Schema(description = "医生姓名")
    private String doctorName;

    @Schema(description = "医生编码")
    private String doctorCode;

    @Schema(description = "职称")
    private String title;

    @Schema(description = "擅长领域")
    private String specialty;

    @Schema(description = "医生简介")
    private String description;

    @Schema(description = "头像URL")
    private String avatar;

    @Schema(description = "挂号费（元）")
    private BigDecimal price;

    @Schema(description = "从业年限")
    private Integer experience;

    @Schema(description = "学历")
    private String education;

    @Schema(description = "状态")
    private Integer status;

    @Schema(description = "可预约日期")
    private LocalDate nextAvailableDate;
}
