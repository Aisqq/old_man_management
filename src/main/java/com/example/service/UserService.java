package com.example.service;

import com.example.pojo.Result;
import com.example.pojo.User;

public interface UserService {
    Result save(User user);

    Result resetPassword(User user);
}
