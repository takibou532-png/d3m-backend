package com.menu.demo.Controllers;



import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.menu.demo.Models.User;

import com.menu.demo.Services.SchoolRequestService;


import Dto.SchoolRegistrationRequestDto;
import Dto.SchoolRequestResponseDto;
import Dto.SchoolResponseDto;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/school-requests")
@RequiredArgsConstructor
public class SchoolRequestController {

    private final SchoolRequestService schoolRequestService;

    // Public — school owner submits registration request
    @PostMapping
    public ResponseEntity<SchoolRequestResponseDto> submit(
            @RequestBody SchoolRegistrationRequestDto request) {
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(schoolRequestService.submitRequest(request));
    }

    // Super admin views all pending requests (paginated)
    // GET /api/school-requests/pending?page=0&size=10
    @GetMapping("/pending")
    public ResponseEntity<Page<SchoolRequestResponseDto>> getPending(
            @AuthenticationPrincipal User currentUser,
            Pageable pageable) {
        return ResponseEntity.ok(schoolRequestService.getPendingRequests(pageable));
    }

    // Super admin approves a request → creates School + Admin account
    @PostMapping("/{id}/approve")
    public ResponseEntity<SchoolResponseDto> approve(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(schoolRequestService.approveRequest(id, currentUser));
    }

    // Super admin rejects a request with a comment
    @PostMapping("/{id}/reject")
    public ResponseEntity<Void> reject(
            @PathVariable Long id,
            @RequestParam(required = false) String comment,
            @AuthenticationPrincipal User currentUser) {
        schoolRequestService.rejectRequest(id, comment, currentUser);
        return ResponseEntity.ok().build();
    }
}
