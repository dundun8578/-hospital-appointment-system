package com.hospital.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.hospital.user.entity.User;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 *
 * @author hospital
 * @date 2024-01-15
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // BaseMapper已提供基础CRUD方法
}
