package com.guman.bbc_backend.auth;

import com.guman.bbc_backend.entity.Employee;
import com.guman.bbc_backend.repository.EmployeeRepository;
import com.guman.bbc_backend.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

@Service
public class LoginService {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Autowired
    private EmailService emailService;

    private Map<String, String> otpStore = new HashMap<>();
    private Map<String, EmployeeSession> sessionStore = new HashMap<>();

    // ... existing code ...

    public LoginResponse login(LoginRequest loginRequest) {
        LoginResponse response = new LoginResponse();
        
        Employee employee = employeeRepository.findByEmail(loginRequest.getEmail())
                .orElse(null);

        if (employee == null) {
            // New employee, generate and send OTP
            String otp = generateOtp();
            otpStore.put(loginRequest.getEmail(), otp);
            emailService.sendOtpEmail(loginRequest.getEmail(), otp);
            response.setMessage("OTP sent to your email");
            response.setNewUser(true);
        } else if (loginRequest.getOtp() != null && loginRequest.getOtp().equals(otpStore.get(loginRequest.getEmail()))) {
            EmployeeSession session = createSession(employee);
            response.setToken(session.getToken());
            response.setMessage("Login successful");
            otpStore.remove(loginRequest.getEmail());
        } else {
            response.setMessage("Invalid credentials");
        }
        return response;
    }

    public LoginResponse verifyOtp(String email, String otp) {
        LoginResponse response = new LoginResponse();
        
        if (otp.equals(otpStore.get(email))) {
            Employee newEmployee = new Employee();
            newEmployee.setEmail(email);
            newEmployee.setName("New User");
            newEmployee.setCreatedAt(LocalDateTime.now());
            employeeRepository.save(newEmployee);

            EmployeeSession session = createSession(newEmployee);
            response.setToken(session.getToken());
            response.setMessage("OTP verified and account created");
            otpStore.remove(email);
        } else {
            response.setMessage("Invalid OTP");
        }
        return response;
    }

    private String generateOtp() {
        Random random = new Random();
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public EmployeeSession createSession(Employee employee) {
        EmployeeSession session = new EmployeeSession();
        session.setEmployeeId(employee.getEmployeeId());
        session.setToken(generateSessionToken());
        session.setExpirationTime(LocalDateTime.now().plusHours(1)); // Session expires in 1 hour
        sessionStore.put(session.getToken(), session);
        return session;
    }

    private String generateSessionToken() {
        return UUID.randomUUID().toString();
    }

    public boolean isValidSession(String token) {
        EmployeeSession session = sessionStore.get(token);
        return session != null && session.isValid();
    }

    public void invalidateSession(String token) {
        sessionStore.remove(token);
    }
}