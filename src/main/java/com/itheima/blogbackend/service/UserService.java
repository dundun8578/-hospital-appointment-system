package com.itheima.blogbackend.service;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.itheima.blogbackend.mapper.UserMapper;
import org.springframework.stereotype.Service;

/**
 * 用户服务类
 */
@Service
public class UserService extends ServiceImpl<UserMapper, User> {

    // ServiceImpl已经继承了BaseMapper的所有方法
    // 可以在这里添加自定义业务逻辑
}
