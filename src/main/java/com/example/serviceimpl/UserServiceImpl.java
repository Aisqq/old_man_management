package com.example.serviceimpl;

import com.example.dao.UserDao;
import com.example.pojo.Result;
import com.example.pojo.User;
import com.example.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class UserServiceImpl implements UserService {
    @Autowired
    private UserDao userDao;
    @Override
    public Result save(User user) {
        if(user.getUsername().length()>8){
            return new Result(false,"用户名不能超过8位");
        }
        if(userDao.findByUserPhone(user.getPhone())!=null){
            return new Result(false,"手机号已存在");
        }
        boolean exists = userDao.existsByUsername(user.getUsername());
        if (exists) {
            return new Result(false,"用户已存在");
        }
        userDao.save(user);
        return new Result(true,"注册成功");
    }

    @Override
    public Result resetPassword(User user) {
        userDao.updatePassword(user.getUsername(),user.getPassword());
        return new Result(true,"密码已修改");
    }
}
