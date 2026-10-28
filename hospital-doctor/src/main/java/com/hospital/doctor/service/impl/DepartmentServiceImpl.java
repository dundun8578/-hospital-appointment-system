package com.hospital.doctor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.doctor.entity.Department;
import com.hospital.doctor.mapper.DepartmentMapper;
import com.hospital.doctor.service.DepartmentService;
import com.hospital.doctor.vo.DepartmentVO;
import com.hospital.doctor.mapper.DoctorMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 科室服务实现类
 *
 * @author hospital
 * @date 2024-01-15
 */
@Slf4j
@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    @Autowired
    private DoctorMapper doctorMapper;

    /**
     * 根据医院ID查询科室列表
     *
     * @param hospitalId 医院ID
     * @return 科室列表
     */
    @Override
    public List<DepartmentVO> getDepartmentList(Long hospitalId) {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getHospitalId, hospitalId)
               .eq(Department::getStatus, 1)
               .eq(Department::getDeleted, 0)
               .orderByAsc(Department::getParentId);

        List<Department> departments = baseMapper.selectList(wrapper);

        return departments.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 根据医院ID查询一级科室
     *
     * @param hospitalId 医院ID
     * @return 一级科室列表
     */
    @Override
    public List<DepartmentVO> getTopLevelDepartments(Long hospitalId) {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getHospitalId, hospitalId)
               .eq(Department::getParentId, 0)
               .eq(Department::getStatus, 1)
               .eq(Department::getDeleted, 0)
               .orderByAsc(Department::getDepartmentName);

        List<Department> departments = baseMapper.selectList(wrapper);
        return departments.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 根据父科室ID查询子科室
     *
     * @param parentId 父科室ID
     * @return 子科室列表
     */
    @Override
    public List<DepartmentVO> getSubDepartments(Long parentId) {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Department::getParentId, parentId)
               .eq(Department::getStatus, 1)
               .eq(Department::getDeleted, 0)
               .orderByAsc(Department::getDepartmentName);

        List<Department> departments = baseMapper.selectList(wrapper);
        return departments.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 实体转VO
     *
     * @param department 科室实体
     * @return 科室VO
     */
    private DepartmentVO convertToVO(Department department) {
        DepartmentVO vo = new DepartmentVO();
        org.springframework.beans.BeanUtils.copyProperties(department, vo);

        // 查询该科室下的医生数量
        vo.setDoctorCount(doctorMapper.countActiveByDepartment(department.getDepartmentId()));

        return vo;
    }
}
