package com.hospital.doctor.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.doctor.entity.Hospital;
import com.hospital.doctor.mapper.HospitalMapper;
import com.hospital.doctor.service.HospitalService;
import com.hospital.doctor.vo.HospitalVO;
import com.hospital.doctor.mapper.DepartmentMapper;
import com.hospital.doctor.mapper.DoctorMapper;
import org.springframework.cache.annotation.Cacheable;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 医院服务实现类
 *
 * @author hospital
 * @date 2024-01-15
 */
@Slf4j
@Service
public class HospitalServiceImpl extends ServiceImpl<HospitalMapper, Hospital> implements HospitalService {

    @Autowired
    private DepartmentMapper departmentMapper;

    @Autowired
    private DoctorMapper doctorMapper;

    /**
     * 查询医院列表
     *
     * @param province 省份
     * @param city     城市
     * @param level    医院等级
     * @return 医院列表
     */
    @Override
    @Cacheable(cacheNames = "hospital:list", key = "(#province ?: '') + ':' + (#city ?: '') + ':' + (#level ?: '')")
    public List<HospitalVO> getHospitalList(String province, String city, String level) {
        LambdaQueryWrapper<Hospital> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Hospital::getStatus, 1)
               .eq(Hospital::getDeleted, 0);

        if (province != null && !province.isEmpty()) {
            wrapper.like(Hospital::getProvince, province);
        }
        if (city != null && !city.isEmpty()) {
            wrapper.like(Hospital::getCity, city);
        }
        if (level != null && !level.isEmpty()) {
            wrapper.like(Hospital::getLevel, level);
        }

        List<Hospital> hospitals = baseMapper.selectList(wrapper);

        return hospitals.stream().map(this::convertToVO).collect(Collectors.toList());
    }

    /**
     * 根据ID查询医院详情
     *
     * @param hospitalId 医院ID
     * @return 医院VO
     */
    @Override
    @Cacheable(cacheNames = "hospital:detail", key = "#hospitalId")
    public HospitalVO getHospitalDetail(Long hospitalId) {
        Hospital hospital = baseMapper.selectById(hospitalId);
        if (hospital == null) {
            return null;
        }
        HospitalVO vo = convertToVO(hospital);

        // 查询科室数量和医生数量
        vo.setDepartmentCount(departmentMapper.countActiveByHospital(hospitalId));
        vo.setDoctorCount(doctorMapper.countActiveByHospital(hospitalId));

        return vo;
    }

    /**
     * 实体转VO
     *
     * @param hospital 医院实体
     * @return 医院VO
     */
    private HospitalVO convertToVO(Hospital hospital) {
        HospitalVO vo = new HospitalVO();
        org.springframework.beans.BeanUtils.copyProperties(hospital, vo);
        return vo;
    }
}
