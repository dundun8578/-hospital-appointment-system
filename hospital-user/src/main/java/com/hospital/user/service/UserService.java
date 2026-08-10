package com.hospital.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.hospital.user.dto.LoginDTO;
import com.hospital.user.dto.RegisterDTO;
import com.hospital.user.entity.User;
import com.hospital.user.vo.LoginVO;
import com.hospital.user.vo.UserVO;

/**
 * 用户服务接口
 *
 * @author hospital
 * @date 2024-01-15
 */
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 登录信息（含Token）
     */
    LoginVO register(RegisterDTO registerDTO);

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 登录信息（含Token）
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    User findByUsername(String username);

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    User findByPhone(String phone);

    /**
     * 根据ID查询用户信息（VO）
     *
     * @param userId 用户ID
     * @return 用户VO
     */
    UserVO getUserInfo(Long userId);

    /**
     * 校验手机号是否已存在
     *
     * @param phone 手机号
     * @return true-存在，false-不存在
     */
    boolean checkPhoneExists(String phone);

    /**
     * 校验用户名是否已存在
     *
     * @param username 用户名
     * @return true-存在，false-不存在
     */
    boolean checkUsernameExists(String username);
}
