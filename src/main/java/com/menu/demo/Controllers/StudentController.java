package com.menu.demo.Controllers;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.menu.demo.Exceptions.ResourceNotFoundException;
import com.menu.demo.Models.SchoolAdminProfile;
import com.menu.demo.Models.User;
import com.menu.demo.Repositories.SchoolAdminProfileRepository;

import com.menu.demo.Services.StudentService;


import Dto.StudentRegistrationRequest;
import Dto.StudentRequestDto;
import Dto.StudentResponseDto;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/students")
@RequiredArgsConstructor
public class StudentController {

    private final StudentService studentService;
    private final SchoolAdminProfileRepository adminProfileRepository;

    // Public — student self-registers
    @PostMapping("/register")
    public ResponseEntity<StudentResponseDto> register(
            @RequestBody StudentRegistrationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(studentService.studentRegistration(request));
    }

    // Student views their own profile
    @GetMapping("/profile")
    public ResponseEntity<StudentResponseDto> getMyProfile(
            @AuthenticationPrincipal User currentUser) {
        return studentService.getCurrentStudentProfile(currentUser);
    }

    // Student updates their own profile
    @PutMapping("/profile")
    public ResponseEntity<StudentResponseDto> updateMyProfile(
            @RequestBody StudentRequestDto request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(studentService.updateCurrentStudentProfile(currentUser, request));
    }

    // School admin or teacher gets all students in a module (attendance sheet)
    @GetMapping("/by-module/{moduleId}")
    public ResponseEntity<List<StudentResponseDto>> getByModule(
            @PathVariable Long moduleId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(studentService.getStudentsByModule(moduleId, resolveAdmin(currentUser)));
    }

    private SchoolAdminProfile resolveAdmin(User user) {
        return adminProfileRepository.findByUser(user)
            .orElseThrow(() -> new ResourceNotFoundException("Admin profile not found"));
    }
    @GetMapping
public ResponseEntity<List<StudentResponseDto>> getAllStudents(
        @AuthenticationPrincipal User currentUser) {
    return ResponseEntity.ok(
        studentService.getAllStudents(resolveAdmin(currentUser)));
}
}
