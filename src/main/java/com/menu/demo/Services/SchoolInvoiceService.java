package com.menu.demo.Services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.menu.demo.Enums.AdminActionType;
import com.menu.demo.Enums.InvoiceStatus;
import com.menu.demo.Enums.SubscriptionStatus;
import com.menu.demo.Exceptions.ResourceNotFoundException;
import com.menu.demo.Models.School;
import com.menu.demo.Models.SchoolSubscriptionInvoice;
import com.menu.demo.Models.SuperAdminAction;
import com.menu.demo.Models.User;
import com.menu.demo.Repositories.SchoolAdminProfileRepository;
import com.menu.demo.Repositories.SchoolRepository;
import com.menu.demo.Repositories.SchoolSubscriptionInvoiceRepository;
import com.menu.demo.Repositories.SuperAdminActionRepository;
import com.menu.demo.Repositories.UserRepository;

import Dto.PlatformDashboardDto;
import Dto.SchoolInvoiceResponseDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SchoolInvoiceService {

    private final SchoolSubscriptionInvoiceRepository invoiceRepository;
    private final SchoolRepository schoolRepository;
    private final SuperAdminActionRepository actionRepository;
    private final SchoolAdminProfileRepository adminProfileRepository;
    private final UserRepository userRepository;

    // ======== SCHEDULER — runs every year on September 1st (start of Algerian academic year) ========

    @Scheduled(cron = "0 0 0 1 9 *")       // 1st September at midnight every year
    public void generateYearlySchoolInvoices() {

        int currentYear = LocalDate.now().getYear();
        String academicYear = currentYear + "/" + (currentYear + 1);
        LocalDate dueDate = LocalDate.of(currentYear, 10, 1);   // due October 1st

        List<School> activeSchools =
            schoolRepository.findAllBySubscriptionStatus(SubscriptionStatus.ACTIVE);

        List<SchoolSubscriptionInvoice> invoices = new ArrayList<>();

        for (School school : activeSchools) {

            // Safety check — don't generate twice
            if (invoiceRepository.existsBySchoolAndYear(school, currentYear))
                continue;

            invoices.add(SchoolSubscriptionInvoice.builder()
                .school(school)
                .year(currentYear)
                .academicYear(academicYear)
                
                .amount(school.getYearlyPrice())
                .status(InvoiceStatus.PENDING)
                .dueDate(dueDate)
                .build());
        }

        invoiceRepository.saveAll(invoices);
    }

    // ======== SCHEDULER — mark overdue daily at 2am ========

    @Scheduled(cron = "0 0 2 * * *")
    public void markOverdueInvoices() {
        List<SchoolSubscriptionInvoice> overdue =
            invoiceRepository.findAllOverdue(LocalDate.now());
        overdue.forEach(i -> i.setStatus(InvoiceStatus.OVERDUE));
        invoiceRepository.saveAll(overdue);
    }

    // ======== SCHEDULER — expire schools that didn't pay (runs daily at 3am) ========

    @Scheduled(cron = "0 0 3 * * *")
    public void expireUnpaidSchools() {

        List<School> expiring =
            schoolRepository.findAllExpiringToday(LocalDate.now());

        for (School school : expiring) {
            school.setSubscriptionStatus(SubscriptionStatus.EXPIRED);
            schoolRepository.save(school);

            // Disable school admin login
            adminProfileRepository.findBySchool(school)
                .ifPresent(profile -> {
                    profile.getUser().setEnabled(false);
                    userRepository.save(profile.getUser());
                });
        }
    }

    // ======== SUPER ADMIN — mark invoice as paid ========

    public SchoolInvoiceResponseDto markAsPaid(Long invoiceId, User superAdmin) {

        SchoolSubscriptionInvoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));

        if (invoice.getStatus() == InvoiceStatus.PAID)
            throw new IllegalStateException("Invoice already paid");

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(LocalDateTime.now());
        invoiceRepository.save(invoice);

        // Extend school subscription by 1 academic year
        School school = invoice.getSchool();
        school.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        school.setSubscriptionExpiresAt(
            LocalDate.of(invoice.getYear() + 1, 8, 31)   // expires end of August next year
        );
        schoolRepository.save(school);

        actionRepository.save(SuperAdminAction.builder()
            .superAdmin(superAdmin)
            .actionType(AdminActionType.MARK_INVOICE_PAID)
            .targetEntityId(invoiceId)
            .targetEntityType("SchoolSubscriptionInvoice")
            .comment("Academic year: " + invoice.getAcademicYear())
            .build());

        return mapToResponse(invoice);
    }

    // ======== SUPER ADMIN — platform dashboard ========

    public PlatformDashboardDto getPlatformDashboard() {

        int currentYear = LocalDate.now().getYear();

        long totalSchools      = schoolRepository.count();
        long activeSchools     = schoolRepository.findAllBySubscriptionStatus(SubscriptionStatus.ACTIVE).size();
        long suspendedSchools  = schoolRepository.findAllBySubscriptionStatus(SubscriptionStatus.SUSPENDED).size();
        long trialSchools      = schoolRepository.findAllBySubscriptionStatus(SubscriptionStatus.TRIAL).size();
        long expiredSchools    = schoolRepository.findAllBySubscriptionStatus(SubscriptionStatus.EXPIRED).size();

        BigDecimal currentYearRevenue = invoiceRepository.sumPaidByYear(currentYear);
        BigDecimal allTimeRevenue     = invoiceRepository.sumAllPaid();

        List<SchoolInvoiceResponseDto> currentYearInvoices =
            invoiceRepository.findAllByYear(currentYear)
                .stream()
                .map(this::mapToResponse)
                .toList();

        return PlatformDashboardDto.builder()
            .totalSchools(totalSchools)
            .activeSchools(activeSchools)
            .suspendedSchools(suspendedSchools)
            .trialSchools(trialSchools)
            .expiredSchools(expiredSchools)
            .currentYearRevenue(currentYearRevenue)
            .allTimeRevenue(allTimeRevenue)
            .recentInvoices(currentYearInvoices)
            .build();
    }

    // ======== GET INVOICES FOR ONE SCHOOL ========

    public List<SchoolInvoiceResponseDto> getInvoicesBySchool(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("School not found: " + schoolId));
        return invoiceRepository.findAllBySchoolOrderByYearDesc(school)
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    private SchoolInvoiceResponseDto mapToResponse(SchoolSubscriptionInvoice i) {
        return SchoolInvoiceResponseDto.builder()
            .id(i.getId())
            .schoolName(i.getSchool().getSchoolName())
            .academicYear(i.getAcademicYear())
            
            .amount(i.getAmount())
            .status(i.getStatus())
            .dueDate(i.getDueDate())
            .paidAt(i.getPaidAt())
            .build();
    }
}
