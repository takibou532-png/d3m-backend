package com.menu.demo.Controllers;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.menu.demo.Models.User;
import com.menu.demo.Services.SchoolService;

import Dto.SchoolResponseDto;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/schools")
@RequiredArgsConstructor
public class SchoolController {

    private final SchoolService schoolService;

    // Super admin views all schools (paginated, excludes EXPIRED)
    @GetMapping
    public ResponseEntity<Page<SchoolResponseDto>> getAll(Pageable pageable) {
        return ResponseEntity.ok(schoolService.getAllSchools(pageable));
    }

    // Get one school by ID
    @GetMapping("/{id}")
    public ResponseEntity<SchoolResponseDto> getById(@PathVariable Long id) {
        return ResponseEntity.ok(schoolService.getSchoolById(id));
    }

    // Super admin suspends a school (disables admin login)
    @PostMapping("/{id}/suspend")
    public ResponseEntity<Void> suspend(
            @PathVariable Long id,
            @RequestParam(required = false) String comment,
            @AuthenticationPrincipal User currentUser) {
        schoolService.suspendSchool(id, comment, currentUser);
        return ResponseEntity.ok().build();
    }

    // Super admin reactivates a suspended school
    @PostMapping("/{id}/reactivate")
    public ResponseEntity<Void> reactivate(
            @PathVariable Long id,
            @RequestParam(required = false) String comment,
            @AuthenticationPrincipal User currentUser) {
        schoolService.reactivateSchool(id, comment, currentUser);
        return ResponseEntity.ok().build();
    }
}