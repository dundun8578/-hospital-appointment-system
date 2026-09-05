package com.childrenhealth.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.childrenhealth.entity.User;
import com.childrenhealth.vo.UserLoginVO;
import com.childrenhealth.vo.UserRegisterVO;

/**
 * 用户服务接口
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param user 用户信息
     * @return 注册结果
     */
    UserRegisterVO register(User user);

    /**
     * 用户登录
     *
     * @param username 用户名
     * @param password 密码
     * @return 登录结果
     */
    UserLoginVO login(String username, String password);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    User getByUsername(String username);

    /**
     * 检查用户名是否存在
     *
     * @param username 用户名
     * @return true=存在，false=不存在
     */
    boolean existsByUsername(String username);

    /**
     * 检查邮箱是否存在
     *
     * @param email 邮箱
     * @return true=存在，false=不存在
     */
    boolean existsByEmail(String email);

    /**
     * 检查手机号是否存在
     *
     * @param phone 手机号
     * @return true=存在，false=不存在
     */
    boolean existsByPhone(String phone);
}
