package com.slim.blogbackend.controller;

import com.slim.blogbackend.dto.request.UserLoginDTO;
import com.slim.blogbackend.dto.request.UserRegisterDTO;
import com.slim.blogbackend.dto.response.UserResponseDTO;
import com.slim.blogbackend.service.UserService;
import com.slim.blogbackend.vo.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @Autowired
    private UserService userService;

    @PostMapping("/register")
    public Result<Map<String, Object>> register(@Valid @RequestBody UserRegisterDTO dto) {
        return userService.register(dto);
    }

    @PostMapping("/login")
    public Result<Map<String, Object>> login(@Valid @RequestBody UserLoginDTO dto) {
        return userService.login(dto);
    }

    @GetMapping("/info")
    public Result<UserResponseDTO> getUserInfo(HttpServletRequest request) {
        Long userId = (Long) request.getAttribute("userId");
        return userService.getUserById(userId);
    }

    @GetMapping("/list")
    public Result<?> getUserList(@RequestParam(defaultValue = "1") int page,
                                 @RequestParam(defaultValue = "10") int size) {
        return userService.getUserList(page, size);
    }
}
