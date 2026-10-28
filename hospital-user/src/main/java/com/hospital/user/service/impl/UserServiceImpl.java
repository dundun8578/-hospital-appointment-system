package com.hospital.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.hospital.common.jwt.JwtUtil;
import com.hospital.common.utils.PasswordEncoderUtil;
import com.hospital.common.BusinessException;
import com.hospital.common.ResultCode;
import com.hospital.user.dto.LoginDTO;
import com.hospital.user.dto.RegisterDTO;
import com.hospital.user.entity.User;
import com.hospital.user.mapper.UserMapper;
import com.hospital.user.service.UserService;
import com.hospital.user.vo.LoginVO;
import com.hospital.user.vo.UserVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.time.LocalDate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

/**
 * 用户服务实现类
 *
 * @author hospital
 * @date 2024-01-15
 */
@Slf4j
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService, UserDetailsService {

    @Autowired
    private PasswordEncoderUtil passwordEncoderUtil;

    @Autowired
    private JwtUtil jwtUtil;

    /**
     * Spring Security加载用户
     *
     * @param username 用户名
     * @return UserDetails
     * @throws UsernameNotFoundException 用户不存在异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = findByUsername(username);
        if (user == null) {
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        // 适配Spring Security的UserDetails（登录校验由JwtInterceptor承担，此方法供扩展使用）
        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword() == null ? "" : user.getPassword())
                .accountLocked(user.getStatus() == null || user.getStatus() != 1)
                .build();
    }

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 登录信息
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public LoginVO register(RegisterDTO registerDTO) {
        log.info("用户注册: {}", registerDTO.getUsername());

        // 1. 校验手机号是否已存在
        if (checkPhoneExists(registerDTO.getPhone())) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "手机号已注册");
        }

        // 2. 校验用户名是否已存在
        if (checkUsernameExists(registerDTO.getUsername())) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "用户名已存在");
        }

        // 3. 校验身份证号是否已存在
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getIdCard, registerDTO.getIdCard());
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "身份证号已注册");
        }

        // 4. 创建用户对象
        User user = new User();
        user.setUsername(registerDTO.getUsername());
        user.setPassword(passwordEncoderUtil.encode(registerDTO.getPassword()));
        user.setRealName(registerDTO.getRealName());
        user.setPhone(registerDTO.getPhone());
        user.setIdCard(registerDTO.getIdCard());
        user.setGender(registerDTO.getGender());
        if (registerDTO.getBirthday() != null && !registerDTO.getBirthday().isEmpty()) {
            user.setBirthday(LocalDate.parse(registerDTO.getBirthday()));
        }
        user.setAddress(registerDTO.getAddress());
        user.setStatus(1);
        user.setRole("USER");

        // 5. 保存到数据库
        baseMapper.insert(user);

        log.info("用户注册成功: userId={}, username={}", user.getUserId(), user.getUsername());

        // 6. 生成Token并返回
        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername(), user.getRole());
        UserVO userVO = convertToVO(user);

        return new LoginVO(token, userVO);
    }

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 登录信息
     */
    @Override
    public LoginVO login(LoginDTO loginDTO) {
        log.info("用户登录: {}", loginDTO.getPhone());

        // 1. 根据手机号查询用户
        User user = findByPhone(loginDTO.getPhone());
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }

        // 2. 校验密码
        if (!passwordEncoderUtil.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException(ResultCode.PARAM_INVALID, "密码错误");
        }

        // 3. 校验用户状态
        if (user.getStatus() != 1) {
            throw new BusinessException(ResultCode.FORBIDDEN, "用户已被禁用");
        }

        // 4. 生成Token
        String token = jwtUtil.generateToken(user.getUserId(), user.getUsername(), user.getRole());

        // 5. 转换为VO
        UserVO userVO = convertToVO(user);

        log.info("用户登录成功: userId={}, username={}", user.getUserId(), user.getUsername());

        return new LoginVO(token, userVO);
    }

    /**
     * 根据用户名查询用户
     *
     * @param username 用户名
     * @return 用户信息
     */
    @Override
    public User findByUsername(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username)
               .eq(User::getDeleted, 0);
        return baseMapper.selectOne(wrapper);
    }

    /**
     * 根据手机号查询用户
     *
     * @param phone 手机号
     * @return 用户信息
     */
    @Override
    public User findByPhone(String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone)
               .eq(User::getDeleted, 0);
        return baseMapper.selectOne(wrapper);
    }

    /**
     * 根据ID查询用户信息（VO）
     *
     * @param userId 用户ID
     * @return 用户VO
     */
    @Override
    public UserVO getUserInfo(Long userId) {
        User user = baseMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException(ResultCode.USER_NOT_FOUND);
        }
        return convertToVO(user);
    }

    /**
     * 校验手机号是否已存在
     *
     * @param phone 手机号
     * @return true-存在，false-不存在
     */
    @Override
    public boolean checkPhoneExists(String phone) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getPhone, phone)
               .eq(User::getDeleted, 0);
        return baseMapper.selectCount(wrapper) > 0;
    }

    /**
     * 校验用户名是否已存在
     *
     * @param username 用户名
     * @return true-存在，false-不存在
     */
    @Override
    public boolean checkUsernameExists(String username) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, username)
               .eq(User::getDeleted, 0);
        return baseMapper.selectCount(wrapper) > 0;
    }

    /**
     * 实体转VO
     *
     * @param user 用户实体
     * @return 用户VO
     */
    private UserVO convertToVO(User user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        return vo;
    }
}
