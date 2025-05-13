package com.example.security;

import com.example.dao.UserDao;
import com.example.pojo.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.List;

@Service
public class CustomUserDetailsService implements UserDetailsService {
    @Autowired
    private  UserDao userDao;
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = null;
        if(username.length()==11){
            user = userDao.findByUserPhone(username);
        }else {
            user = userDao.findByUsername(username);
        }
        if(user==null){
            throw new UsernameNotFoundException("用户不存在: " + username);
        }
        //测试
        System.out.println("用户: " + user.getUsername());
        System.out.println("密码: " + user.getPassword());
        System.out.println("角色: " + user.getRole());

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(convertToAuthorities(user.getRole()))
                .accountExpired(false)
                .accountLocked(false)
                .credentialsExpired(false)
                .disabled(false)
                .build();
    }

    private Collection<? extends GrantedAuthority> convertToAuthorities(String role) {
        String roleName = role.startsWith("ROLE_") ? role : "ROLE_" + role;
        return List.of(new SimpleGrantedAuthority(roleName));
    }

}