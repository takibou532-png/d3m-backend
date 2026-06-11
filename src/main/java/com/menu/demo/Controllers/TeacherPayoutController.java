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

    // School admin — view payout summary for a month
    // GET /api/payouts?period=2025-11
    @GetMapping
    public ResponseEntity<SchoolPayoutSummaryDto> getPayoutSummary(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth period,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
            payoutService.getPayoutSummaryForMonth(period, resolveAdmin(currentUser)));
    }

    // School admin — manually recalculate payouts for a month
    // POST /api/payouts/recalculate?period=2025-11
    @PostMapping("/recalculate")
    public ResponseEntity<SchoolPayoutSummaryDto> recalculate(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth period,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
            payoutService.recalculatePayoutsForMonth(period, resolveAdmin(currentUser)));
    }

    // School admin — mark a payout as paid to the teacher
    // POST /api/payouts/{id}/pay
    @PostMapping("/{id}/pay")
    public ResponseEntity<TeacherPayoutResponseDto> markAsPaid(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
            payoutService.markPayoutAsPaid(id, resolveAdmin(currentUser)));
    }

    // School admin — update a teacher's percentage
    // PATCH /api/payouts/teacher/{teacherId}/percentage?value=20
    @PatchMapping("/teacher/{teacherId}/percentage")
    public ResponseEntity<TeacherResponseDto> updatePercentage(
            @PathVariable Long teacherId,
            @RequestParam BigDecimal value,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(
            payoutService.updateTeacherPercentage(teacherId, value, resolveAdmin(currentUser)));
    }

    // Teacher — view own payouts history
    // GET /api/payouts/mine
    @GetMapping("/mine")
    public ResponseEntity<List<TeacherPayoutResponseDto>> getMyPayouts(
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(payoutService.getMyPayouts(currentUser));
    }

    // Teacher — view own payout for a specific month
    // GET /api/payouts/mine/month?period=2025-11
    @GetMapping("/mine/month")
    public ResponseEntity<TeacherPayoutResponseDto> getMyPayoutForMonth(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM") YearMonth period,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(payoutService.getMyPayoutForMonth(period, currentUser));
    }

    private SchoolAdminProfile resolveAdmin(User user) {
        return adminProfileRepository.findByUser(user)
            .orElseThrow(() -> new ResourceNotFoundException("Admin profile not found"));
    }
}
