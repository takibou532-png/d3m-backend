package com.menu.demo.Controllers;
import java.util.List;

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

import com.menu.demo.Exceptions.ResourceNotFoundException;
import com.menu.demo.Models.SchoolAdminProfile;
import com.menu.demo.Models.User;
import com.menu.demo.Repositories.SchoolAdminProfileRepository;

import com.menu.demo.Services.EnrollmentService;

import Dto.EnrollmentResponseDto;
import Dto.StudentEnrollmentRequestDto;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/enrollments")
@RequiredArgsConstructor
public class EnrollmentController {

    private final EnrollmentService enrollmentService;
    private final SchoolAdminProfileRepository adminProfileRepository;

    // Student requests to join a module
    @PostMapping("/request")
    public ResponseEntity<Void> requestEnrollment(
            @RequestBody StudentEnrollmentRequestDto request,
            @AuthenticationPrincipal User currentUser) {
       
        enrollmentService.requestEnrollment(request, currentUser);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    // School admin approves a pending request → creates Enrollment
    @PostMapping("/requests/{requestId}/approve")
    public ResponseEntity<Void> approve(
            @PathVariable Long requestId,
            @AuthenticationPrincipal User currentUser) throws Exception {
        enrollmentService.approveEnrollment(requestId, resolveAdmin(currentUser));
        return ResponseEntity.ok().build();
    }

    // School admin rejects a pending request
    @PostMapping("/requests/{requestId}/reject")
    public ResponseEntity<Void> reject(
            @PathVariable Long requestId,
            @RequestParam(required = false) String comment,
            @AuthenticationPrincipal User currentUser) throws Exception {
        enrollmentService.rejectEnrollment(requestId, comment, resolveAdmin(currentUser));
        return ResponseEntity.ok().build();
    }

    // Student views their active enrollments
    @GetMapping("/mine")
    public ResponseEntity<List<EnrollmentResponseDto>> getMyEnrollments(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(enrollmentService.getMyEnrollments(currentUser));
    }

    // Student cancels a PENDING enrollment request
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