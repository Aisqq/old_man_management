package com.example.controller;

import com.example.dto.RegisterRequest;
import com.example.pojo.Result;
import com.example.pojo.User;
import com.example.service.CaptchaService;
import com.example.service.UserService;
import com.example.utils.UserHolder;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    @Autowired
    private UserService userService;
    @Autowired
    private CaptchaService captchaService;
    /**
     * 用户注册
     **/
    @PostMapping("/public/register")
    public Result register(@RequestBody RegisterRequest registerRequest) {
        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setPassword(bCryptPasswordEncoder.encode(registerRequest.getPassword()));
        user.setRole("ROLE_USER");
        user.setPhone(registerRequest.getPhone());
        return userService.save(user);
    }
    /**
     * 重置密码
     **/
    @PostMapping("/resetPassword")
    public Result resetPassword(@RequestBody Map<String, String> request) {
        User user = UserHolder.getUser();
        String password = request.get("password");
        if(password==null||password.isBlank()||password.length()<8){
            return new Result(false,"密码不能小于6位");
        }
        user.setPassword(bCryptPasswordEncoder.encode(password));
       return userService.resetPassword(user);
    }
    /**
     * 发送重置验证码
     **/
    @PostMapping("/public/sendCode")
    public Result sendCode(String phone){
       return captchaService.sendCode(phone);
    }

    /**
     *检验验证码
     * 检验成功，签名（用于后续修改密码）
     */

    @PostMapping("/public/verifyCode")
    public Result verifyCode(@RequestBody Map<String, String> params, HttpServletResponse response) throws IOException {
        String phone = params.get("phone");
        String code = params.get("code");
        return captchaService.verifyCode(phone,code,response);
    }
}
