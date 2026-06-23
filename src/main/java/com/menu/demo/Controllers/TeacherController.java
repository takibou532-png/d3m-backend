package com.menu.demo.Controllers;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
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


import com.menu.demo.Services.TeacherService;


import Dto.TeacherRequestDto;
import Dto.TeacherResponseDto;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/teachers")
@RequiredArgsConstructor
public class TeacherController {

    private final TeacherService teacherService;
    private final SchoolAdminProfileRepository adminProfileRepository;

    // School admin creates a teacher
    @PostMapping("/create")
    public ResponseEntity<TeacherResponseDto> create(
            @RequestBody TeacherRequestDto request,
            @AuthenticationPrincipal User currentUser) throws Exception {
        
        return teacherService.creacteTeacherProfile(request, resolveAdmin(currentUser));
    }

    // School admin gets all teachers in their school
    @GetMapping
    public ResponseEntity<List<TeacherResponseDto>> getBySchool(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(teacherService.getTeachersBySchool(resolveAdmin(currentUser)));
    }

    // Teacher views their own profile
    @GetMapping("/profile")
    public ResponseEntity<TeacherResponseDto> getMyProfile(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(teacherService.getCurrentTeacherProfile(currentUser));
    }

    // Admin  updates their own profile
    @PutMapping("/profile")
    public ResponseEntity<TeacherResponseDto> updateMyProfile(
            @RequestBody TeacherRequestDto request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(teacherService.updateCurrentTeacherProfile(currentUser, request));
    }

    // School admin archives a teacher
    @PatchMapping("/{id}/archive")
    public ResponseEntity<Void> archive(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        teacherService.archiveTeacher(id, resolveAdmin(currentUser));
        return ResponseEntity.noContent().build();
    }

    private SchoolAdminProfile resolveAdmin(User user) {
        return adminProfileRepository.findByUser(user)
            .orElseThrow(() -> new ResourceNotFoundException("Admin profile not found"));
    }
    // GET /api/teachers/archived
@GetMapping("/archived")
public ResponseEntity<List<TeacherResponseDto>> getArchived(
        @AuthenticationPrincipal User currentUser) {
    return ResponseEntity.ok(teacherService.getArchivedTeachers(resolveAdmin(currentUser)));
}

// PATCH /api/teachers/{id}/unarchive
@PatchMapping("/{id}/unarchive")
public ResponseEntity<Void> unarchive(
        @PathVariable Long id,
        @AuthenticationPrincipal User currentUser) {
    teacherService.unarchiveTeacher(id, resolveAdmin(currentUser));
    return ResponseEntity.noContent().build();
}
}
