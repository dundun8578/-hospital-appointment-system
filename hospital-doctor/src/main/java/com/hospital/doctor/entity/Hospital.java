package com.hospital.doctor.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 医院实体类
 * 对应数据库表：hospital
 *
 * @author hospital
 * @date 2024-01-15
 */
@Data
@TableName("hospital")
public class Hospital implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 医院ID，主键
     */
    @TableId(value = "hospital_id", type = IdType.AUTO)
    private Long hospitalId;

    /**
     * 医院名称
     */
    private String hospitalName;

    /**
     * 医院编码
     */
    private String hospitalCode;

    /**
     * 省份
     */
    private String province;

    /**
     * 城市
     */
    private String city;

    /**
     * 区县
     */
    private String district;

    /**
     * 详细地址
     */
    private String address;

    /**
     * 联系电话
     */
    private String phone;

    /**
     * 医院等级
     */
    private String level;

    /**
     * 医院类型：1-综合医院 2-专科医院 3-中医医院
     */
    private Integer type;

    /**
     * 医院简介
     */
    private String description;

    /**
     * 医院logo
     */
    private String logo;

    /**
     * 状态：1-正常 0-停用
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
}
