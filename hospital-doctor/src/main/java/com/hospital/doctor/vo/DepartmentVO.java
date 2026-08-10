package com.hospital.doctor.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;

/**
 * 科室VO
 *
 * @author hospital
 * @date 2024-01-15
 */
@Data
@Schema(description = "科室信息")
public class DepartmentVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Schema(description = "科室ID")
    private Long departmentId;

    @Schema(description = "医院ID")
    private Long hospitalId;

    @Schema(description = "科室名称")
    private String departmentName;

    @Schema(description = "科室编码")
    private String departmentCode;

    @Schema(description = "父科室ID")
    private Long parentId;

    @Schema(description = "科室描述")
    private String description;

    @Schema(description = "科室位置")
    private String location;

    @Schema(description = "医生数量")
    private Integer doctorCount;
}
