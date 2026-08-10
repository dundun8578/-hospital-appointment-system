package com.hospital.doctor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.doctor.entity.Doctor;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 医生Mapper接口
 *
 * @author hospital
 * @date 2024-01-15
 */
@Mapper
public interface DoctorMapper extends BaseMapper<Doctor> {

    /**
     * 根据科室ID查询医生列表（包含医院和科室信息）
     *
     * @param departmentId 科室ID
     * @return 医生列表
     */
    List<Doctor> selectListWithHospitalInfo(@Param("departmentId") Long departmentId);

    /**
     * 根据医院ID查询医生列表
     *
     * @param hospitalId 医院ID
     * @return 医生列表
     */
    List<Doctor> selectListByHospitalId(@Param("hospitalId") Long hospitalId);
}
