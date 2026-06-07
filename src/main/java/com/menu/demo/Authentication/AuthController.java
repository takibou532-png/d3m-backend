package com.menu.demo.Authentication;

import java.util.Arrays;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.menu.demo.Exceptions.ResourceNotFoundException;
import com.menu.demo.Models.User;
import com.menu.demo.Repositories.UserRepository;
import com.menu.demo.SecurityJwt.JwtUtill;
import com.menu.demo.Services.UserService;

import Dto.StudentRegistrationRequest;
import Dto.StudentResponseDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserService userService;
    private final UserRepository userRepository;
    private final JwtUtill jwtutil;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthenticationRequest loginRequest,HttpServletResponse response) {
        // 1️⃣ Authenticate credentials
        try {
            UsernamePasswordAuthenticationToken authInputToken =
                    new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword());
            authenticationManager.authenticate(authInputToken);
        } catch (Exception e) {
            e.printStackTrace();  // <-- log the reason
            return ResponseEntity.status(403).body("Login failed: " + e.getMessage());
        }

       
        UserDetails userDetails = userService.loadUserByUsername(loginRequest.getEmail());

        // 3️⃣ Generate JWT tokens
        String accessToken = jwtutil.generateAccessToken(userDetails);
        String refreshToken = jwtutil.generateRefreshToken(userDetails);
        // 4️⃣ Return token ( set in cookie)
        String cookie = String.format(
                "refreshToken=%s; HttpOnly; Path=/; Max-Age=%d; SameSite=None; Secure",
                refreshToken, 7 * 24 * 60 * 60
            );
        
            response.addHeader("Set-Cookie", cookie);

        return ResponseEntity.ok(Map.of("AccessToken",accessToken));
    }
    // ========================= Refresh =====================
    @PostMapping("/refresh")
    public ResponseEntity<?> refresh(HttpServletRequest request) {

        String refreshToken = Arrays.stream(request.getCookies())
                .filter(c -> c.getName().equals("refreshToken"))
                .findFirst()
                .map(Cookie::getValue)
                .orElse(null);

        if (refreshToken == null || !jwtutil.isRefreshToken(refreshToken)) {
            return ResponseEntity.status(403).body("Invalid refresh token");
        }

        String email = jwtutil.extractEmail(refreshToken);
        UserDetails user = userService.loadUserByUsername(email);

        if (!jwtutil.isTokenValid(refreshToken, user)) {
            return ResponseEntity.status(403).body("Expired refresh token");
        }

        String newAccessToken = jwtutil.generateAccessToken(user);

        return ResponseEntity.ok(
                Map.of("accessToken", newAccessToken)
        );
    }

    
    //==============  Change password  ======================= 
    @PutMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePassword request,
            Authentication auth) {

        String email = auth.getName();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found"));

        // 1) verify old password
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Old password is incorrect");
        }

        // 2) encode new password
        String encodedNew = passwordEncoder.encode(request.getNewPassword());

        // 3) save new password
        user.setPassword(encodedNew);
       userRepository.save(user);

        return ResponseEntity.ok("Password updated successfully");
    }     
    
    
    
    //======================= Log Out ============================
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {

        response.addHeader(
            "Set-Cookie",
            "refreshToken=; HttpOnly; Path=/; Max-Age=0; SameSite=None; Secure"
        );

        return ResponseEntity.ok("Logged out");
    }



 // ================================= me function ==============================================
    @GetMapping("/me")
    public ResponseEntity<?> getUserInfo(Authentication auth) {
        String email = auth.getName();
        User user = userRepository.findByEmail(email)
            .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        return ResponseEntity.ok(Map.of(
            "id",       user.getId(),
            "fullName", user.getFullName(),
            "email",    user.getEmail(),
            "role",     user.getRole()
            
        ));
    }


}