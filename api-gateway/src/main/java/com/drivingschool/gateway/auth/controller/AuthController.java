package com.drivingschool.gateway.auth.controller;

import com.drivingschool.common.dto.ApiResult;
import com.drivingschool.gateway.auth.dto.LoginRequest;
import com.drivingschool.gateway.auth.dto.LoginResponse;
import com.drivingschool.gateway.auth.dto.admin.RegisterAdminRequest;
import com.drivingschool.gateway.auth.dto.instructor.RegisterInstructorRequest;
import com.drivingschool.gateway.auth.dto.RegisterResponse;
import com.drivingschool.gateway.auth.dto.student.RegisterStudentRequest;
import com.drivingschool.gateway.auth.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register/student")
    public Mono<ResponseEntity<ApiResult<RegisterResponse>>> registerStudent(@Valid @RequestBody RegisterStudentRequest request) {
        return authService.registerStudent(request)
                .map(response -> ResponseEntity.ok(ApiResult.success("Student account registered successfully", response)));
    }

    @PostMapping("/register/instructor")
    public Mono<ResponseEntity<ApiResult<RegisterResponse>>> registerInstructor(@Valid @RequestBody RegisterInstructorRequest request) {
        return authService.registerInstructor(request)
                .map(response -> ResponseEntity.ok(ApiResult.success("Instructor account registered successfully", response)));
    }

    @PostMapping("/register/admin")
    public ResponseEntity<ApiResult<RegisterResponse>> registerAdmin(@Valid @RequestBody RegisterAdminRequest request) {
        RegisterResponse response = authService.registerAdmin(request);
        return ResponseEntity.ok(ApiResult.success("Admin account registered successfully", response));
    }

    @PostMapping("/login")
    public ResponseEntity<ApiResult<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(ApiResult.success("Authenticated successfully", response));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResult<String>> logout() {
        // Stateless JWT: client drops token. Server-side token revocation can be added later with a blocklist store.
        return ResponseEntity.ok(ApiResult.success("Logged out. Remove the token on client side.", null));
    }
}

