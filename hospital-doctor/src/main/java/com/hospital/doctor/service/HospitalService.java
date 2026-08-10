package com.hospital.doctor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.doctor.dto.ScheduleCreateDTO;
import com.hospital.doctor.dto.ScheduleQueryDTO;
import com.hospital.doctor.entity.Department;
import com.hospital.doctor.entity.Doctor;
import com.hospital.doctor.entity.Hospital;
import com.hospital.doctor.entity.Schedule;
import com.hospital.doctor.vo.DoctorVO;
import com.hospital.doctor.vo.HospitalVO;
import com.hospital.doctor.vo.ScheduleVO;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * 医院服务接口
 *
 * @author hospital
 * @date 2024-01-15
 */
public interface HospitalService extends IService<Hospital> {

    /**
     * 查询医院列表
     *
     * @param province 省份
     * @param city     城市
     * @param level    医院等级
     * @return 医院列表
     */
    List<HospitalVO> getHospitalList(String province, String city, String level);

    /**
     * 根据ID查询医院详情
     *
     * @param hospitalId 医院ID
     * @return 医院VO
     */
    HospitalVO getHospitalDetail(Long hospitalId);
}
