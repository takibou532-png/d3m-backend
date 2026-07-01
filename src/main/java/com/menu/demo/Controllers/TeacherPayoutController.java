package com.menu.demo.Controllers;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.menu.demo.Exceptions.ResourceNotFoundException;
import com.menu.demo.Models.SchoolAdminProfile;
import com.menu.demo.Models.User;
import com.menu.demo.Repositories.SchoolAdminProfileRepository;
import com.menu.demo.Services.TeacherPayoutService;

import Dto.SchoolPayoutSummaryDto;
import Dto.TeacherPayoutResponseDto;
import Dto.TeacherResponseDto;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/payouts")
@RequiredArgsConstructor
public class TeacherPayoutController {

    private final TeacherPayoutService payoutService;
    private final SchoolAdminProfileRepository adminProfileRepository;

    // GET /api/payouts?period=2025-11
    @GetMapping
    public ResponseEntity<SchoolPayoutSummaryDto> getPayoutSummary(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth period,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
            payoutService.getPayoutSummaryForMonth(period, resolveAdmin(currentUser)));
    }

    // POST /api/payouts/recalculate?period=2025-11
    @PostMapping("/recalculate")
    public ResponseEntity<SchoolPayoutSummaryDto> recalculate(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth period,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
            payoutService.recalculatePayoutsForMonth(period, resolveAdmin(currentUser)));
    }

    // POST /api/payouts/{id}/pay
    @PostMapping("/{id}/pay")
    public ResponseEntity<TeacherPayoutResponseDto> markAsPaid(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
            payoutService.markPayoutAsPaid(id, resolveAdmin(currentUser)));
    }

    // PATCH /api/payouts/teacher/{teacherId}/percentage?value=20
    @PatchMapping("/teacher/{teacherId}/percentage")
    public ResponseEntity<TeacherResponseDto> updatePercentage(
            @PathVariable Long teacherId,
            @RequestParam BigDecimal value,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
            payoutService.updateTeacherPercentage(teacherId, value, resolveAdmin(currentUser)));
    }

    // GET /api/payouts/mine
    @GetMapping("/mine")
    public ResponseEntity<List<TeacherPayoutResponseDto>> getMyPayouts(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(payoutService.getMyPayouts(currentUser));
    }

    // GET /api/payouts/mine/month?period=2025-11
    @GetMapping("/mine/month")
    public ResponseEntity<TeacherPayoutResponseDto> getMyPayoutForMonth(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth period,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(payoutService.getMyPayoutForMonth(period, currentUser));
    }

    // GET /api/payouts/teacher/{teacherId}/latest
    // Used by the edit modal to show previous payout amount in DA
    @GetMapping("/teacher/{teacherId}/latest")
    public ResponseEntity<TeacherPayoutResponseDto> getLatestForTeacher(
            @PathVariable Long teacherId,
            @AuthenticationPrincipal User currentUser) {

        TeacherPayoutResponseDto dto =
            payoutService.getLatestPayoutForTeacher(teacherId, resolveAdmin(currentUser));

        // 204 when no payout history — frontend shows "لا توجد دفعات بعد"
        return dto != null
            ? ResponseEntity.ok(dto)
            : ResponseEntity.noContent().build();
    }

    private SchoolAdminProfile resolveAdmin(User user) {
        return adminProfileRepository.findByUser(user)
            .orElseThrow(() -> new ResourceNotFoundException("Admin profile not found"));
    }
}