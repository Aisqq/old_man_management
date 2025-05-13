package com.example.dao;

import com.example.pojo.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;
import org.springframework.cache.annotation.Cacheable;

@Mapper
public interface UserDao {
    @Cacheable(value = "findUserByName",key = "#username")
    @Select("SELECT * FROM users WHERE  BINARY username = #{username}")
    User findByUsername(@Param("username") String username);


    @Select("SELECT EXISTS(SELECT 1 FROM users WHERE username = #{username})")
    boolean existsByUsername(@Param("username") String username);

    @Select("INSERT INTO users(username,password,role,phone) VALUES(#{username},#{password},#{role},#{phone})")
    void save(User user);

    @Cacheable(value = "findUserByPhone",key = "#username")
    @Select("SELECT * FROM users WHERE  BINARY phone = #{phone}")
    User findByUserPhone(@Param("phone") String phone);
    @Update("UPDATE users SET password = #{password} WHERE username = #{username}")
    void updatePassword(@Param("username") String username, @Param("password") String password);
}
