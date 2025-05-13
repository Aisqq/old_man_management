package com.example.serviceimpl;

import com.example.dao.UserDao;
import com.example.pojo.Result;
import com.example.pojo.User;
import com.example.service.CaptchaService;
import com.example.utils.CaptchaUtil;
import com.example.utils.JwtTokenUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

@Service
public class CaptchaServiceImpl implements CaptchaService {
    // 验证码有效期（分钟）
    private static final long CAPTCHA_EXPIRE_TIME = 3;
    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private UserDao userDao;
    @Override

    public void generateCaptcha(HttpServletRequest request,HttpServletResponse response) throws IOException {
        // 1. 生成随机验证码
        String captchaText = CaptchaUtil.generateRandomText(4);
        // 2. 存储到Redis（key格式：captcha:sessionId）
        String sessionId = request.getSession().getId();
        System.out.println();
        System.out.printf("sessionId:开始"+sessionId);

        String redisKey = "captcha:" + sessionId;
        stringRedisTemplate.opsForValue().set(
                redisKey,
                captchaText,
                CAPTCHA_EXPIRE_TIME,
                TimeUnit.MINUTES
        );

        // 3. 生成图片并返回
        BufferedImage image = CaptchaUtil.generateCaptchaImage(captchaText, 120, 40);
        response.setContentType("image/png");
        ImageIO.write(image, "png", response.getOutputStream());
    }

    @Override
    public boolean validateCaptcha(HttpServletRequest request, String userInput) {
        String sessionId = request.getSession().getId();
        String redisKey = "captcha:" + sessionId;

        // 从Redis获取验证码
        String correctCaptcha = stringRedisTemplate.opsForValue().get(redisKey);

        // 校验逻辑（忽略大小写）
        boolean isValid = correctCaptcha != null &&
                correctCaptcha.equalsIgnoreCase(userInput);
        // 验证后立即删除（一次性使用）
        if (isValid) {
            stringRedisTemplate.delete(redisKey);
        }
        return isValid;
    }

    @Override
    public Result sendCode(String phone) {
        if(userDao.findByUserPhone(phone)==null){
            return new Result(false,"用户未注册");
        }
        String code = CaptchaUtil.generateRandomCode(6);
        stringRedisTemplate.opsForValue().set(
                "code:"+phone,
                code,
                CAPTCHA_EXPIRE_TIME,
                TimeUnit.MINUTES
        );
        //这里发送验证码

        System.out.println(code);
        return new Result(true,"发送成功");
    }

    @Override
    public Result verifyCode(String phone, String code,HttpServletResponse response) throws IOException {
        String storedCode = stringRedisTemplate.opsForValue().get("code:" + phone);
        System.out.println(storedCode);
        if (storedCode == null||!storedCode.equals(code)) {
            return new Result(false,"检验失败");
        }
        stringRedisTemplate.delete("code:" + phone);
        User user = userDao.findByUserPhone(phone);
        String token = jwtTokenUtil.generateToken(user);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        Cookie cookie = new Cookie("jwtToken",token);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setMaxAge(1000);
        cookie.setPath("/");
        response.addCookie(cookie);
        return new Result(true,"验证码校验成功");
    }

}
