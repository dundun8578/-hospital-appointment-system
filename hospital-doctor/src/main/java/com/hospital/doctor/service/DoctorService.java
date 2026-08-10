package com.hospital.doctor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.doctor.entity.Doctor;
import com.hospital.doctor.vo.DoctorVO;

import java.util.List;

/**
 * 医生服务接口
 *
 * @author hospital
 * @date 2024-01-15
 */
public interface DoctorService extends IService<Doctor> {

    /**
     * 根据科室ID查询医生列表
     *
     * @param departmentId 科室ID
     * @return 医生列表
     */
    List<DoctorVO> getDoctorListByDepartment(Long departmentId);

    /**
     * 根据医生ID查询医生详情
     *
     * @param doctorId 医生ID
     * @return 医生VO
     */
    DoctorVO getDoctorDetail(Long doctorId);

    /**
     * 根据医院ID查询医生列表
     *
     * @param hospitalId 医院ID
     * @return 医生列表
     */
    List<DoctorVO> getDoctorListByHospital(Long hospitalId);

    /**
     * 搜索医生（按姓名、擅长领域）
     *
     * @param keyword 关键词
     * @return 医生列表
     */
    List<DoctorVO> searchDoctors(String keyword);
}
