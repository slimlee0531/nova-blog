package com.slim.blogbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.slim.blogbackend.dto.request.UserLoginDTO;
import com.slim.blogbackend.dto.request.UserRegisterDTO;
import com.slim.blogbackend.dto.response.UserResponseDTO;
import com.slim.blogbackend.entity.User;
import com.slim.blogbackend.exception.BusinessException;
import com.slim.blogbackend.mapper.UserMapper;
import com.slim.blogbackend.util.JwtUtil;
import com.slim.blogbackend.vo.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private JwtUtil jwtUtil;

    @Transactional
    public Result<Map<String, Object>> register(UserRegisterDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername())
                .or()
                .eq(User::getEmail, dto.getEmail());

        if (userMapper.selectCount(wrapper) > 0) {
            throw new BusinessException("用户名或邮箱已存在");
        }

        User user = User.builder()
                .username(dto.getUsername())
                .email(dto.getEmail())
                .passwordHash(dto.getPassword())
                .displayName(dto.getDisplayName() != null ? dto.getDisplayName() : dto.getUsername())
                .role(User.UserRole.AUTHOR.name())
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        userMapper.insert(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", toResponseDTO(user));

        return Result.success(data);
    }

    public Result<Map<String, Object>> login(UserLoginDTO dto) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, dto.getUsername());

        User user = userMapper.selectOne(wrapper);

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        if (!user.getIsActive()) {
            throw new BusinessException("账号已被禁用");
        }

        if (!user.getPasswordHash().equals(dto.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        user.setLastLoginAt(LocalDateTime.now());
        userMapper.updateById(user);

        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());

        Map<String, Object> data = new HashMap<>();
        data.put("token", token);
        data.put("user", toResponseDTO(user));

        return Result.success(data);
    }

    public Result<UserResponseDTO> getUserById(Long id) {
        User user = userMapper.selectById(id);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        return Result.success(toResponseDTO(user));
    }

    public Result<Page<UserResponseDTO>> getUserList(int page, int size) {
        Page<User> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.orderByDesc(User::getCreatedAt);

        Page<User> result = userMapper.selectPage(pageParam, wrapper);

        Page<UserResponseDTO> responsePage = new Page<>(result.getCurrent(), result.getSize(), result.getTotal());
        responsePage.setRecords(result.getRecords().stream()
                .map(this::toResponseDTO)
                .toList());

        return Result.success(responsePage);
    }

    private UserResponseDTO toResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .displayName(user.getDisplayName())
                .avatarUrl(user.getAvatarUrl())
                .bio(user.getBio())
                .role(user.getRole())
                .isActive(user.getIsActive())
                .createdAt(user.getCreatedAt())
                .build();
    }
}
