package com.menu.demo.Controllers;
import java.time.YearMonth;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.menu.demo.Exceptions.ResourceNotFoundException;
import com.menu.demo.Models.SchoolAdminProfile;
import com.menu.demo.Models.User;
import com.menu.demo.Repositories.SchoolAdminProfileRepository;

import com.menu.demo.Services.InvoiceService;


import Dto.SchoolRevenueDto;
import Dto.StudentInvoiceResponseDto;
import lombok.RequiredArgsConstructor;
@RestController
@RequestMapping("/api/invoices")
@RequiredArgsConstructor
public class InvoiceController {

    private final InvoiceService invoiceService;
    private final SchoolAdminProfileRepository adminProfileRepository;

    // School admin marks a student invoice as paid
    @PostMapping("/{id}/pay")
    public ResponseEntity<StudentInvoiceResponseDto> markAsPaid(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) throws Exception {
        return ResponseEntity.ok(invoiceService.markAsPaid(id, resolveAdmin(currentUser)));
    }

    // School admin views revenue for a given month
    // GET /api/invoices/school/revenue?period=2025-03
    @GetMapping("/school/revenue")
    public ResponseEntity<SchoolRevenueDto> getSchoolRevenue(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth period,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(invoiceService.getSchoolRevenueByMonth(period, resolveAdmin(currentUser)));
    }

    // Student views their own invoices
    @GetMapping("/mine")
    public ResponseEntity<List<StudentInvoiceResponseDto>> getMyInvoices(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(invoiceService.getMyInvoices(currentUser));
    }

    private SchoolAdminProfile resolveAdmin(User user) {
        return adminProfileRepository.findByUser(user)
            .orElseThrow(() -> new ResourceNotFoundException("Admin profile not found"));
    }
}
