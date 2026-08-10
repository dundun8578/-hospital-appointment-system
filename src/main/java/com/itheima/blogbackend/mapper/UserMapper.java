package com.itheima.blogbackend.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

/**
 * 用户Mapper接口
 */
@Mapper
public interface UserMapper extends BaseMapper<User> {
    // BaseMapper已经提供了基本的CRUD方法
    // 可以在这里添加自定义方法
}
