package com.menu.demo.Controllers;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.menu.demo.Exceptions.ResourceNotFoundException;
import com.menu.demo.Models.SchoolAdminProfile;
import com.menu.demo.Models.User;
import com.menu.demo.Repositories.SchoolAdminProfileRepository;
import com.menu.demo.Services.CourseModuleService;

import Dto.CourseModuleRequestDto;
import Dto.CourseModuleResponseDto;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
public class CoursModuleController {

    private final CourseModuleService courseModuleService;
    private final SchoolAdminProfileRepository adminProfileRepository;

    @PostMapping
    public ResponseEntity<CourseModuleResponseDto> create(
            @RequestBody CourseModuleRequestDto request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(courseModuleService.createCourseModule(request, resolveAdmin(currentUser)));
    }

    @GetMapping
    public ResponseEntity<List<CourseModuleResponseDto>> getBySchool(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(courseModuleService.getModulesBySchool(resolveAdmin(currentUser)));
    }

    @GetMapping("/browse")
    public ResponseEntity<List<CourseModuleResponseDto>> browse(
            @RequestParam Long schoolId,
            @RequestParam String level) {
        return ResponseEntity.ok(courseModuleService.browseModules(schoolId, level));
    }

    // ── NEW: update module ──────────────────────────────────────────────────
    @PutMapping("/{id}")
    public ResponseEntity<CourseModuleResponseDto> update(
            @PathVariable Long id,
            @RequestBody CourseModuleRequestDto request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
            courseModuleService.updateCourseModule(id, request, resolveAdmin(currentUser)));
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<Void> archive(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        courseModuleService.archiveModule(id, resolveAdmin(currentUser));
        return ResponseEntity.noContent().build();
    }

    private SchoolAdminProfile resolveAdmin(User user) {
        return adminProfileRepository.findByUser(user)
            .orElseThrow(() -> new ResourceNotFoundException("Admin profile not found"));
    }
}