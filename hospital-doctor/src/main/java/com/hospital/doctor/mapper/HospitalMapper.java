package com.hospital.doctor.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.doctor.entity.Hospital;
import org.apache.ibatis.annotations.Mapper;

/**
 * 医院Mapper接口
 *
 * @author hospital
 * @date 2024-01-15
 */
@Mapper
public interface HospitalMapper extends BaseMapper<Hospital> {
}
