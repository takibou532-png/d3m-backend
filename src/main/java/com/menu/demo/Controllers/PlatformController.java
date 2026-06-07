package com.menu.demo.Controllers;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.menu.demo.Models.User;
import com.menu.demo.Services.SchoolInvoiceService;

import Dto.PlatformDashboardDto;
import Dto.SchoolInvoiceResponseDto;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/platform")
@RequiredArgsConstructor
public class PlatformController {

    private final SchoolInvoiceService schoolInvoiceService;

    // Super admin views the full platform dashboard
    @GetMapping("/dashboard")
    public ResponseEntity<PlatformDashboardDto> getDashboard() {
        return ResponseEntity.ok(schoolInvoiceService.getPlatformDashboard());
    }

    // Super admin views all yearly invoices for a specific school
    @GetMapping("/invoices/school/{schoolId}")
    public ResponseEntity<List<SchoolInvoiceResponseDto>> getBySchool(
            @PathVariable Long schoolId) {
        return ResponseEntity.ok(schoolInvoiceService.getInvoicesBySchool(schoolId));
    }

    // Super admin marks a school subscription invoice as paid
    @PostMapping("/invoices/{id}/pay")
    public ResponseEntity<SchoolInvoiceResponseDto> markAsPaid(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(schoolInvoiceService.markAsPaid(id, currentUser));
    }
}
