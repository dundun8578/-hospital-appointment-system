package com.hospital.user.controller;

import com.hospital.common.Result;
import com.hospital.user.dto.LoginDTO;
import com.hospital.user.dto.RegisterDTO;
import com.hospital.user.service.UserService;
import com.hospital.user.vo.LoginVO;
import com.hospital.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户控制器
 *
 * @author hospital
 * @date 2024-01-15
 */
@Slf4j
@RestController
@RequestMapping("/user")
@Tag(name = "用户管理", description = "用户注册、登录、信息查询")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 用户注册
     *
     * @param registerDTO 注册信息
     * @return 登录信息（含Token）
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "用户注册并返回Token")
    public Result<LoginVO> register(@Valid @RequestBody RegisterDTO registerDTO) {
        log.info("用户注册: {}", registerDTO.getUsername());
        LoginVO loginVO = userService.register(registerDTO);
        return Result.success(loginVO);
    }

    /**
     * 用户登录
     *
     * @param loginDTO 登录信息
     * @return 登录信息（含Token）
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "用户登录并返回Token")
    public Result<LoginVO> login(@Valid @RequestBody LoginDTO loginDTO) {
        log.info("用户登录: {}", loginDTO.getPhone());
        LoginVO loginVO = userService.login(loginDTO);
        return Result.success(loginVO);
    }

    /**
     * 获取当前用户信息
     *
     * @return 用户信息
     */
    @GetMapping("/info")
    @Operation(summary = "获取当前用户信息", description = "需要登录")
    public Result<UserVO> getCurrentUserInfo() {
        // 从ThreadLocal获取当前用户ID
        Long userId = com.hospital.auth.interceptor.UserContextHolder.getUserId();
        log.info("获取用户信息: userId={}", userId);

        UserVO userVO = userService.getUserInfo(userId);
        return Result.success(userVO);
    }

    /**
     * 校验手机号是否可用
     *
     * @param phone 手机号
     * @return 是否可用
     */
    @GetMapping("/check-phone")
    @Operation(summary = "校验手机号", description = "检查手机号是否已注册")
    public Result<Boolean> checkPhone(@RequestParam String phone) {
        boolean exists = userService.checkPhoneExists(phone);
        return Result.success(!exists);
    }

    /**
     * 校验用户名是否可用
     *
     * @param username 用户名
     * @return 是否可用
     */
    @GetMapping("/check-username")
    @Operation(summary = "校验用户名", description = "检查用户名是否已存在")
    public Result<Boolean> checkUsername(@RequestParam String username) {
        boolean exists = userService.checkUsernameExists(username);
        return Result.success(!exists);
    }
}
