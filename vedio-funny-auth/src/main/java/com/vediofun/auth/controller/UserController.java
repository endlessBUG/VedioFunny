package com.vediofun.auth.controller;

import com.vediofun.auth.entity.User;
import com.vediofun.auth.repository.UserRepository;
import com.vediofun.common.result.Result;
import com.vediofun.auth.util.PasswordUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    private final UserRepository userRepository;

    @GetMapping("/list")
    public Result<?> list(@RequestParam(name = "page", defaultValue = "1") int page,
                         @RequestParam(name = "size", defaultValue = "10") int size,
                         @RequestParam(name = "username", required = false) String username) {
        Pageable pageable = PageRequest.of(page - 1, size);
        User probe = new User();
        if (username != null && !username.isEmpty()) probe.setUsername(username);
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withMatcher("username", m -> m.contains());
        Example<User> example = Example.of(probe, matcher);
        Page<User> userPage = userRepository.findAll(example, pageable);
        return Result.success(userPage);
    }

    @DeleteMapping("/delete/{id}")
    public Result<?> delete(@PathVariable Long id) {
        userRepository.deleteById(id);
        return Result.success();
    }

    @PostMapping("/edit")
    public Result<?> edit(@RequestBody User user) {
        User dbUser = userRepository.findById(user.getId()).orElseThrow();
        dbUser.setEmail(user.getEmail());
        dbUser.setStatus(user.getStatus());
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            dbUser.setPassword(PasswordUtil.encode(user.getPassword()));
        }
        userRepository.save(dbUser);
        return Result.success();
    }
} 