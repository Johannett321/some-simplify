package com.templateapp.templateapp.rest;

import com.templateapp.api.AuthApi;
import com.templateapp.model.CreateUserCommand;
import com.templateapp.model.LoginUserCommand;
import com.templateapp.templateapp.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AuthApiImpl implements AuthApi {

    private final UserService userService;

    @Override
    public ResponseEntity<Void> login(LoginUserCommand loginUserCommand) {
        userService.loginUser(loginUserCommand);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> register(CreateUserCommand createUserCommand) {
        userService.createUserWithBasicAuth(createUserCommand);
        return ResponseEntity.ok().build();
    }

    @Override
    public ResponseEntity<Void> logout() {
        userService.logout();
        return ResponseEntity.ok().build();
    }
}
