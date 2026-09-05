package com.childrenhealth.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.childrenhealth.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // BaseMapper已提供基础CRUD方法
    // 如需自定义SQL，可在此添加方法
}
