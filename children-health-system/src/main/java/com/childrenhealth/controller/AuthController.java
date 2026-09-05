package com.childrenhealth.controller;

import com.childrenhealth.common.Result;
import com.childrenhealth.entity.User;
import com.childrenhealth.service.UserService;
import com.childrenhealth.vo.UserLoginVO;
import com.childrenhealth.vo.UserRegisterVO;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证控制器
 * 提供用户注册和登录接口
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Validated
public class AuthController {

    private final UserService userService;

    /**
     * 用户注册接口
     * POST /api/auth/register
     *
     * @param request 注册请求
     * @return 注册结果
     */
    @PostMapping("/register")
    public Result<UserRegisterVO> register(@Valid @RequestBody RegisterRequest request) {
        // 构建User对象
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole(request.getRole());

        // 调用服务层注册
        UserRegisterVO result = userService.register(user);

        return Result.success("注册成功", result);
    }

    /**
     * 用户登录接口
     * POST /api/auth/login
     *
     * @param request 登录请求
     * @return 登录结果
     */
    @PostMapping("/login")
    public Result<UserLoginVO> login(@Valid @RequestBody LoginRequest request) {
        UserLoginVO result = userService.login(request.getUsername(), request.getPassword());
        return Result.success("登录成功", result);
    }

    /**
     * 注册请求DTO
     */
    public static class RegisterRequest {

        /**
         * 用户名（3-20位，字母数字下划线）
         */
        @NotBlank(message = "用户名不能为空")
        @Size(min = 3, max = 20, message = "用户名长度必须在3-20位之间")
        @Pattern(regexp = "^[a-zA-Z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
        private String username;

        /**
         * 密码（6-20位）
         */
        @NotBlank(message = "密码不能为空")
        @Size(min = 6, max = 20, message = "密码长度必须在6-20位之间")
        private String password;

        /**
         * 邮箱
         */
        @Email(message = "邮箱格式不正确")
        private String email;

        /**
         * 手机号（中国大陆手机号格式）
         */
        @Pattern(regexp = "^1[3-9]\\d{9}$", message = "手机号格式不正确")
        private String phone;

        /**
         * 角色：1=家长，2=医生，3=管理员（默认1）
         */
        private Integer role = 1;

        // Getter和Setter
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public Integer getRole() {
            return role;
        }

        public void setRole(Integer role) {
            this.role = role;
        }
    }

    /**
     * 登录请求DTO
     */
    public static class LoginRequest {

        /**
         * 用户名
         */
        @NotBlank(message = "用户名不能为空")
        private String username;

        /**
         * 密码
         */
        @NotBlank(message = "密码不能为空")
        private String password;

        // Getter和Setter
        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }
    }
}
