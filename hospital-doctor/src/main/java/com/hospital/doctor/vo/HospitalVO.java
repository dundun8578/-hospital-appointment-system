package com.hospital.doctor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 医院VO
 *
 * @author hospital
 * @date 2024-01-15
 */
@Data
@Schema(description = "医院信息")
public class HospitalVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "医院ID")
    private Long hospitalId;

    @Schema(description = "医院名称")
    private String hospitalName;

    @Schema(description = "医院编码")
    private String hospitalCode;

    @Schema(description = "省份")
    private String province;

    @Schema(description = "城市")
    private String city;

    @Schema(description = "区县")
    private String district;

    @Schema(description = "详细地址")
    private String address;

    @Schema(description = "联系电话")
    private String phone;

    @Schema(description = "医院等级")
    private String level;

    @Schema(description = "医院类型")
    private Integer type;

    @Schema(description = "医院简介")
    private String description;

    @Schema(description = "医院logo")
    private String logo;

    @Schema(description = "科室数量")
    private Integer departmentCount;

    @Schema(description = "医生数量")
    private Integer doctorCount;
}
