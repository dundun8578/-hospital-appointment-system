package com.hospital.doctor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.doctor.entity.Department;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 科室Mapper接口
 *
 * @author hospital
 * @date 2024-01-15
 */
@Mapper
public interface DepartmentMapper extends BaseMapper<Department> {

    /**
     * 根据医院ID查询科室列表（包含医生数量）
     *
     * @param hospitalId 医院ID
     * @return 科室列表
     */
    List<Department> selectListWithDoctorCount(@Param("hospitalId") Long hospitalId);
}
