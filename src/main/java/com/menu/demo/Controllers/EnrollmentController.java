package com.menu.demo.Controllers;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.menu.demo.Enums.EnrollmentStatus;
import com.menu.demo.Exceptions.ResourceNotFoundException;
import com.menu.demo.Models.SchoolAdminProfile;
import com.menu.demo.Models.User;
import com.menu.demo.Repositories.SchoolAdminProfileRepository;

import com.menu.demo.Services.EnrollmentService;

import Dto.EnrollmentResponseDto;
import Dto.StudentEnrollmentRequestDto;
import Dto.StudentRequestResponseDto;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final SchoolAdminProfileRepository adminProfileRepository;

    // ── School admin views all incoming requests ──────────────────
    // GET /api/enrollments/requests?status=PENDING&page=0&size=10
    // GET /api/enrollments/requests?page=0&size=10  (all statuses)
    @GetMapping("/requests")
    public ResponseEntity<Page<StudentRequestResponseDto>> getSchoolRequests(
            @RequestParam(required = false) EnrollmentStatus status,
            Pageable pageable,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
            enrollmentService.getSchoolRequests(status, pageable, resolveAdmin(currentUser)));
    }

    // ── School admin views requests for one specific module ───────
    // GET /api/enrollments/requests/module/1?status=PENDING
    @GetMapping("/requests/module/{moduleId}")
    public ResponseEntity<List<StudentRequestResponseDto>> getRequestsByModule(
            @PathVariable Long moduleId,
            @RequestParam(required = false) EnrollmentStatus status,
            @AuthenticationPrincipal User currentUser) {

        return ResponseEntity.ok(
            enrollmentService.getRequestsByModule(moduleId, status, resolveAdmin(currentUser)));
    }

    // ── Dashboard badge — count of pending requests ───────────────
    // GET /api/enrollments/requests/count
    @GetMapping("/requests/count")
    public ResponseEntity<Map<String, Long>> countPending(
            @AuthenticationPrincipal User currentUser) {

        long count = enrollmentService.countPendingRequests(resolveAdmin(currentUser));
        return ResponseEntity.ok(Map.of("pendingCount", count));
    }

    // ── School admin approves a request ──────────────────────────
    // POST /api/enrollments/requests/{id}/approve
    @PostMapping("/requests/{requestId}/approve")
    public ResponseEntity<Void> approve(
            @PathVariable Long requestId,
            @AuthenticationPrincipal User currentUser) throws Exception {
        enrollmentService.approveEnrollment(requestId, resolveAdmin(currentUser));
        return ResponseEntity.ok().build();
    }

    // ── School admin rejects a request ───────────────────────────
    // POST /api/enrollments/requests/{id}/reject?comment=Niveau+insuffisant
    @PostMapping("/requests/{requestId}/reject")
    public ResponseEntity<Void> reject(
            @PathVariable Long requestId,
            @RequestParam(required = false) String comment,
            @AuthenticationPrincipal User currentUser) throws Exception {
        enrollmentService.rejectEnrollment(requestId, comment, resolveAdmin(currentUser));
        return ResponseEntity.ok().build();
    }

    // ── Student sends enrollment request ─────────────────────────
    @PostMapping("/request")
    public ResponseEntity<Void> requestEnrollment(
            @RequestBody StudentEnrollmentRequestDto request,
            @AuthenticationPrincipal User currentUser) {
        enrollmentService.requestEnrollment(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // ── Student views their own enrollments ──────────────────────
    @GetMapping("/mine")
    public ResponseEntity<List<EnrollmentResponseDto>> getMyEnrollments(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(enrollmentService.getMyEnrollments(currentUser));
    }

    // ── Student cancels a pending request ────────────────────────
    @PatchMapping("/{enrollmentId}/cancel")
    public ResponseEntity<Void> cancel(
            @PathVariable Long enrollmentId,
            @AuthenticationPrincipal User currentUser) throws Exception {
        enrollmentService.cancelEnrollment(enrollmentId, currentUser);
        return ResponseEntity.ok().build();
    }

    private SchoolAdminProfile resolveAdmin(User user) {
        return adminProfileRepository.findByUser(user)
            .orElseThrow(() -> new ResourceNotFoundException("Admin profile not found"));
    }
}