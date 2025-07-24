package com.vediofun.auth.mapper;

import com.vediofun.auth.entity.User;
import org.apache.ibatis.annotations.*;

@Mapper
public interface UserMapper {
    
    @Select("SELECT * FROM users WHERE username = #{username}")
    User findByUsername(String username);
    
    @Select("SELECT COUNT(*) FROM users WHERE username = #{username}")
    boolean existsByUsername(String username);
    
    @Select("SELECT COUNT(*) FROM users WHERE email = #{email}")
    boolean existsByEmail(String email);
    
    @Select("SELECT COUNT(*) FROM users WHERE phone = #{phone}")
    boolean existsByPhone(String phone);
    
    @Insert("INSERT INTO users (username, password, email, phone, nickname, avatar, status, user_type, created_time, updated_time) " +
            "VALUES (#{username}, #{password}, #{email}, #{phone}, #{nickname}, #{avatar}, #{status}, #{userType}, NOW(), NOW())")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(User user);
} 