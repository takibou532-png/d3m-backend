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

import com.menu.demo.Services.SubjectService;


import Dto.SubjectResponseDto;
import Dto.SubjectrequestDto;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/subjects")
@RequiredArgsConstructor
public class SubjectController {

    private final SubjectService subjectService;
    private final SchoolAdminProfileRepository adminProfileRepository;

    @PostMapping
    public ResponseEntity<SubjectResponseDto> create(
            @RequestBody SubjectrequestDto request,
            @AuthenticationPrincipal User currentUser) {
        return subjectService.createSubject(request, resolveAdmin(currentUser));
    }

    @GetMapping
    public ResponseEntity<List<SubjectResponseDto>> getAll(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(subjectService.getAllSubjects(resolveAdmin(currentUser)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SubjectResponseDto> update(
            @PathVariable Long id,
            @RequestBody SubjectrequestDto dto,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(subjectService.updateSubject(id, resolveAdmin(currentUser), dto));
    }

    @PatchMapping("/{id}/archive")
    public ResponseEntity<SubjectResponseDto> archive(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return subjectService.archiveSubject(id, resolveAdmin(currentUser));
    }

    private SchoolAdminProfile resolveAdmin(User user) {
        return adminProfileRepository.findByUser(user)
            .orElseThrow(() -> new ResourceNotFoundException("Admin profile not found"));
    }
}
