package com.menu.demo.Services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.menu.demo.Enums.PayoutStatus;
import com.menu.demo.Exceptions.ResourceNotFoundException;
import com.menu.demo.Models.School;
import com.menu.demo.Models.SchoolAdminProfile;
import com.menu.demo.Models.TeacherPayout;
import com.menu.demo.Models.TeacherProfile;
import com.menu.demo.Models.User;
import com.menu.demo.Repositories.InvoiceRepository;
import com.menu.demo.Repositories.TeacherPayoutRepository;
import com.menu.demo.Repositories.TeacherRepository;

import Dto.SchoolPayoutSummaryDto;
import Dto.TeacherPayoutResponseDto;
import Dto.TeacherResponseDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class TeacherPayoutService {

    private final TeacherPayoutRepository payoutRepository;
    private final TeacherRepository teacherRepository;
    private final InvoiceRepository invoiceRepository;

    // ======== SCHEDULER — runs 2nd of every month at midnight ========
    // Calculates payouts for the PREVIOUS month
    // Runs after the 1st (when student invoices are generated and some already paid)

    @Scheduled(cron = "0 0 0 2 * *")
    public void calculateMonthlyPayouts() {

        // Calculate for last month — gives full month for payments to come in
        YearMonth previousMonth = YearMonth.now().minusMonths(1);

        // Process every school separately
        // Get all non-archived teachers with a percentage > 0
        List<TeacherProfile> eligibleTeachers =
            teacherRepository.findAllByArchivedFalseAndPercentageGreaterThan(BigDecimal.ZERO);

        List<TeacherPayout> payouts = new ArrayList<>();

        for (TeacherProfile teacher : eligibleTeachers) {

            // Don't recalculate if already done
            if (payoutRepository.existsByTeacherAndPeriod(teacher, previousMonth))
                continue;

            // How much did students pay for this teacher's modules last month?
            BigDecimal revenue = invoiceRepository
                .sumPaidByTeacherAndPeriod(teacher, previousMonth);

            // Skip teachers with zero revenue (no point creating a 0 payout)
            if (revenue.compareTo(BigDecimal.ZERO) == 0)
                continue;

            // Snapshot the percentage at calculation time
            BigDecimal percentage = teacher.getPercentage();

            // Calculate payout: revenue × percentage / 100
            BigDecimal payoutAmount = revenue
                .multiply(percentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            payouts.add(TeacherPayout.builder()
                .teacher(teacher)
                .school(teacher.getSchool())
                .period(previousMonth)
                .totalModuleRevenue(revenue)
                .percentage(percentage)
                .payoutAmount(payoutAmount)
                .status(PayoutStatus.PENDING)
                .build());
        }

        payoutRepository.saveAll(payouts);
    }

    // ======== SCHOOL ADMIN — manually trigger recalculation for a month ========
    // Useful if late invoice payments come in after the 2nd

    public SchoolPayoutSummaryDto recalculatePayoutsForMonth(
            YearMonth period,
            SchoolAdminProfile admin) {

        School school = admin.getSchool();

        List<TeacherProfile> teachers =
            teacherRepository.findAllBySchoolAndArchivedFalse(school);

        for (TeacherProfile teacher : teachers) {

            if (teacher.getPercentage().compareTo(BigDecimal.ZERO) == 0)
                continue;

            BigDecimal revenue =
                invoiceRepository.sumPaidByTeacherAndPeriod(teacher, period);

            if (revenue.compareTo(BigDecimal.ZERO) == 0)
                continue;

            BigDecimal percentage = teacher.getPercentage();
            BigDecimal payoutAmount = revenue
                .multiply(percentage)
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);

            // Update if exists, create if not
            payoutRepository.findByTeacherAndPeriod(teacher, period)
                .ifPresentOrElse(
                    existing -> {
                        // Only recalculate if not yet paid
                        if (existing.getStatus() == PayoutStatus.PENDING) {
                            existing.setTotalModuleRevenue(revenue);
                            existing.setPercentage(percentage);
                            existing.setPayoutAmount(payoutAmount);
                            existing.setCalculatedAt(LocalDateTime.now());
                            payoutRepository.save(existing);
                        }
                    },
                    () -> payoutRepository.save(TeacherPayout.builder()
                        .teacher(teacher)
                        .school(school)
                        .period(period)
                        .totalModuleRevenue(revenue)
                        .percentage(percentage)
                        .payoutAmount(payoutAmount)
                        .status(PayoutStatus.PENDING)
                        .build())
                );
        }

        return getPayoutSummaryForMonth(period, admin);
    }

    // ======== SCHOOL ADMIN — view all payouts for a month ========

    public SchoolPayoutSummaryDto getPayoutSummaryForMonth(
            YearMonth period,
            SchoolAdminProfile admin) {

        School school = admin.getSchool();

        List<TeacherPayout> payouts =
            payoutRepository.findAllBySchoolAndPeriod(school, period);

        BigDecimal totalDue =
            payoutRepository.sumPendingBySchoolAndPeriod(school, period);

        BigDecimal totalPaid =
            payoutRepository.sumPaidBySchoolAndPeriod(school, period);

        return SchoolPayoutSummaryDto.builder()
            .period(period)
            .totalPayoutsDue(totalDue)
            .totalPayoutsPaid(totalPaid)
            .teacherCount(payouts.size())
            .payouts(payouts.stream().map(this::mapToResponse).toList())
            .build();
    }

    // ======== SCHOOL ADMIN — mark payout as paid to teacher ========

    public TeacherPayoutResponseDto markPayoutAsPaid(Long payoutId, SchoolAdminProfile admin) {

        TeacherPayout payout = payoutRepository.findById(payoutId)
            .orElseThrow(() -> new ResourceNotFoundException("Payout not found: " + payoutId));

        if (!payout.getSchool().getId().equals(admin.getSchool().getId()))
            throw new AccessDeniedException("Payout does not belong to your school");

        if (payout.getStatus() == PayoutStatus.PAID)
            throw new IllegalStateException("Payout already marked as paid");

        payout.setStatus(PayoutStatus.PAID);
        payout.setPaidAt(LocalDateTime.now());
        payoutRepository.save(payout);

        return mapToResponse(payout);
    }

    // ======== SCHOOL ADMIN — update teacher percentage ========

    public TeacherResponseDto updateTeacherPercentage(
            Long teacherId,
            BigDecimal newPercentage,
            SchoolAdminProfile admin) {

        if (newPercentage.compareTo(BigDecimal.ZERO) < 0 ||
            newPercentage.compareTo(BigDecimal.valueOf(100)) > 0)
            throw new IllegalArgumentException("Percentage must be between 0 and 100");

        TeacherProfile teacher = teacherRepository.findById(teacherId)
            .orElseThrow(() -> new ResourceNotFoundException("Teacher not found: " + teacherId));

        if (!teacher.getSchool().getId().equals(admin.getSchool().getId()))
            throw new AccessDeniedException("Teacher does not belong to your school");

        teacher.setPercentage(newPercentage);
        teacherRepository.save(teacher);

        // Note: already-calculated payouts keep their snapshot percentage
        // Only future payouts will use the new percentage

        return mapTeacherToResponse(teacher);
    }

    // ======== TEACHER — view their own payouts ========

    public List<TeacherPayoutResponseDto> getMyPayouts(User currentUser) {

        TeacherProfile teacher = teacherRepository.findByUser(currentUser)
            .orElseThrow(() -> new ResourceNotFoundException("Teacher profile not found"));

        return payoutRepository
            .findAllByTeacherOrderByPeriodDesc(teacher)
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    // ======== TEACHER — view revenue breakdown for a specific month ========

    public TeacherPayoutResponseDto getMyPayoutForMonth(YearMonth period, User currentUser) {

        TeacherProfile teacher = teacherRepository.findByUser(currentUser)
            .orElseThrow(() -> new ResourceNotFoundException("Teacher profile not found"));

        return payoutRepository.findByTeacherAndPeriod(teacher, period)
            .map(this::mapToResponse)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No payout found for period: " + period));
    }

    // ======== MAPPING ========

    private TeacherPayoutResponseDto mapToResponse(TeacherPayout p) {
        return TeacherPayoutResponseDto.builder()
            .id(p.getId())
            .teacherId(p.getTeacher().getId())
            .teacherName(p.getTeacher().getUser().getFullName())
            .period(p.getPeriod())
            .totalModuleRevenue(p.getTotalModuleRevenue())
            .percentage(p.getPercentage())
            .payoutAmount(p.getPayoutAmount())
            .status(p.getStatus())
            .paidAt(p.getPaidAt())
            .calculatedAt(p.getCalculatedAt())
            .build();
    }

    private TeacherResponseDto mapTeacherToResponse(TeacherProfile t) {
        return TeacherResponseDto.builder()
            .id(t.getId())
            .fullName(t.getUser().getFullName())
            .email(t.getUser().getEmail())
            .specialization(t.getSpecialization())
            .bio(t.getBio())
            .schoolId(t.getSchool().getId())
            .archived(t.isArchived())
           
            .build();
    }
}
