package com.guman.bbc_backend.auth;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class LoginController {

    @Autowired
    private LoginService loginService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest loginRequest) {
        return ResponseEntity.ok(loginService.login(loginRequest));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<LoginResponse> verifyOtp(@RequestParam String email, @RequestParam String otp) {
        return ResponseEntity.ok(loginService.verifyOtp(email, otp));
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String token) {
        loginService.invalidateSession(token);
        return ResponseEntity.ok().build();
    }
}