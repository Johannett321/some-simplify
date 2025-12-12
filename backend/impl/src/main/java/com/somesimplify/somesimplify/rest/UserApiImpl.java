package com.somesimplify.somesimplify.rest;

import com.somesimplify.api.UserApi;
import com.somesimplify.model.UserTO;
import com.somesimplify.somesimplify.mapper.UserMapper;
import com.somesimplify.somesimplify.model.User;
import com.somesimplify.somesimplify.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class UserApiImpl implements UserApi {

    private final UserService userService;
    private final UserMapper userMapper;

    @Override
    public ResponseEntity<UserTO> getUser() {
        User currentUser = userService.getCurrentUser();
        return ResponseEntity.ok(userMapper.toUserResponseTO(currentUser));
    }
}
