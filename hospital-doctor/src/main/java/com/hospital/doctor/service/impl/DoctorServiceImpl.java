package com.hospital.doctor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.doctor.entity.Doctor;
import com.hospital.doctor.mapper.DoctorMapper;
import com.hospital.doctor.service.DoctorService;
import com.hospital.doctor.vo.DoctorVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.cache.annotation.Cacheable;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 医生服务实现类
 *
 * @author hospital
 * @date 2024-01-15
 */
@Slf4j
@Service
public class DoctorServiceImpl extends ServiceImpl<DoctorMapper, Doctor> implements DoctorService {

    @Autowired
    private com.hospital.doctor.service.DepartmentService departmentService;

    @Autowired
    private com.hospital.doctor.service.HospitalService hospitalService;

    @Autowired
    @org.springframework.context.annotation.Lazy
    private com.hospital.doctor.service.ScheduleService scheduleService;

    /**
     * 根据科室ID查询医生列表
     *
     * @param departmentId 科室ID
     * @return 医生列表
     */
    @Override
    @Cacheable(cacheNames = "doctor:department", key = "#departmentId")
    public List<DoctorVO> getDoctorListByDepartment(Long departmentId) {
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Doctor::getDepartmentId, departmentId)
               .eq(Doctor::getStatus, 1)
               .eq(Doctor::getDeleted, 0)
               .orderByDesc(Doctor::getExperience);

        List<Doctor> doctors = baseMapper.selectList(wrapper);

        return doctors.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 根据医生ID查询医生详情
     *
     * @param doctorId 医生ID
     * @return 医生VO
     */
    @Override
    @Cacheable(cacheNames = "doctor:detail", key = "#doctorId")
    public DoctorVO getDoctorDetail(Long doctorId) {
        Doctor doctor = baseMapper.selectById(doctorId);
        if (doctor == null) {
            return null;
        }
        DoctorVO vo = convertToVO(doctor);

        // 查询最近可预约日期
        LocalDate nextDate = scheduleService.getNextAvailableDate(doctorId);
        vo.setNextAvailableDate(nextDate);

        return vo;
    }

    /**
     * 根据医院ID查询医生列表
     *
     * @param hospitalId 医院ID
     * @return 医生列表
     */
    @Override
    @Cacheable(cacheNames = "doctor:hospital", key = "#hospitalId")
    public List<DoctorVO> getDoctorListByHospital(Long hospitalId) {
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Doctor::getHospitalId, hospitalId)
               .eq(Doctor::getStatus, 1)
               .eq(Doctor::getDeleted, 0)
               .orderByDesc(Doctor::getExperience);

        List<Doctor> doctors = baseMapper.selectList(wrapper);
        return doctors.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 搜索医生
     *
     * @param keyword 关键词
     * @return 医生列表
     */
    @Override
    public List<DoctorVO> searchDoctors(String keyword) {
        LambdaQueryWrapper<Doctor> wrapper = new LambdaQueryWrapper<>();
        wrapper.and(w -> w.like(Doctor::getDoctorName, keyword)
                         .or()
                         .like(Doctor::getSpecialty, keyword))
               .eq(Doctor::getStatus, 1)
               .eq(Doctor::getDeleted, 0)
               .orderByDesc(Doctor::getExperience)
               .last("LIMIT 20");

        List<Doctor> doctors = baseMapper.selectList(wrapper);
        return doctors.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 实体转VO
     *
     * @param doctor 医生实体
     * @return 医生VO
     */
    private DoctorVO convertToVO(Doctor doctor) {
        DoctorVO vo = new DoctorVO();
        org.springframework.beans.BeanUtils.copyProperties(doctor, vo);

        // 填充医院和科室名称
        if (doctor.getHospitalId() != null) {
            com.hospital.doctor.entity.Hospital hospital = hospitalService.getById(doctor.getHospitalId());
            if (hospital != null) {
                vo.setHospitalName(hospital.getHospitalName());
            }
        }

        if (doctor.getDepartmentId() != null) {
            com.hospital.doctor.entity.Department department = departmentService.getById(doctor.getDepartmentId());
            if (department != null) {
                vo.setDepartmentName(department.getDepartmentName());
            }
        }

        return vo;
    }
}
