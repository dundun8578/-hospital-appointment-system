package com.hospital.doctor.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.doctor.entity.Department;
import com.hospital.doctor.vo.DepartmentVO;

import java.util.List;

/**
 * 科室服务接口
 *
 * @author hospital
 * @date 2024-01-15
 */
public interface DepartmentService extends IService<Department> {

    /**
     * 根据医院ID查询科室列表
     *
     * @param hospitalId 医院ID
     * @return 科室列表
     */
    List<DepartmentVO> getDepartmentList(Long hospitalId);

    /**
     * 根据医院ID查询一级科室
     *
     * @param hospitalId 医院ID
     * @return 一级科室列表
     */
    List<DepartmentVO> getTopLevelDepartments(Long hospitalId);

    /**
     * 根据父科室ID查询子科室
     *
     * @param parentId 父科室ID
     * @return 子科室列表
     */
    List<DepartmentVO> getSubDepartments(Long parentId);
}
