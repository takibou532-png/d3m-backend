package com.menu.demo.Services;

import java.math.BigDecimal;
import java.nio.file.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.menu.demo.Enums.EnrollmentStatus;
import com.menu.demo.Enums.InvoiceStatus;
import com.menu.demo.Enums.Role;
import com.menu.demo.Exceptions.ResourceNotFoundException;
import com.menu.demo.Models.Enrollment;
import com.menu.demo.Models.Invoice;
import com.menu.demo.Models.School;
import com.menu.demo.Models.SchoolAdminProfile;
import com.menu.demo.Models.StudentProfile;
import com.menu.demo.Models.User;
import com.menu.demo.Repositories.EnrollmentRepository;
import com.menu.demo.Repositories.InvoiceRepository;
import com.menu.demo.Repositories.SchoolRepository;
import com.menu.demo.Repositories.StudentRepository;

import Dto.SchoolRevenueDto;
import Dto.StudentInvoiceResponseDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final SchoolRepository schoolRepository;
    private final StudentRepository studentRepository;

    // ======== SCHEDULED JOB — runs at midnight on the 1st of every month ========

    @Scheduled(cron = "0 0 0 1 * *")   // 1st of each month at 00:00
    public void generateMonthlyInvoices() {

        // Generate for the CURRENT month (just started)
        YearMonth period = YearMonth.now();
        LocalDate dueDate = period.atDay(10);   // due on the 10th

        // Get all active enrollments across all schools
        List<Enrollment> activeEnrollments =
            enrollmentRepository.findAllByStatus(EnrollmentStatus.ACCEPTED);

        List<Invoice> invoices = new ArrayList<>();

        for (Enrollment enrollment : activeEnrollments) {

            // Skip if already generated (safety check)
            if (invoiceRepository.existsByEnrollmentAndPeriod(enrollment, period))
                continue;

            // Skip if enrollment ended before this month
            if (enrollment.getEndDate() != null &&
                enrollment.getEndDate().isBefore(period.atDay(1)))
                continue;

            invoices.add(Invoice.builder()
                .enrollment(enrollment)
                .student(enrollment.getStudent())
                .school(enrollment.getModule().getSchool())
                .period(period)
                .totalAmount(enrollment.getMonthlyPrice())   // flat rate from enrollment
                .status(InvoiceStatus.PENDING)
                .dueDate(dueDate)
                .build());
        }

        invoiceRepository.saveAll(invoices);
    }

    // ======== SCHEDULED JOB — marks overdue invoices every day at 1am ========

    @Scheduled(cron = "0 0 1 * * *")
    public void markOverdueInvoices() {

        List<Invoice> overdue =
            invoiceRepository.findAllOverdue(LocalDate.now());

        overdue.forEach(i -> i.setStatus(InvoiceStatus.OVERDUE));

        invoiceRepository.saveAll(overdue);
    }

    // ======== SCHOOL ADMIN MARKS INVOICE AS PAID ========

    public StudentInvoiceResponseDto markAsPaid(Long invoiceId, SchoolAdminProfile admin) throws AccessDeniedException {

     Invoice invoice = invoiceRepository.findById(invoiceId)
            .orElseThrow(() -> new ResourceNotFoundException("Invoice not found: " + invoiceId));

        if (!invoice.getSchool().getId().equals(admin.getSchool().getId()))
            throw new AccessDeniedException("Invoice does not belong to your school");

        if (invoice.getStatus() == InvoiceStatus.PAID)
            throw new IllegalStateException("Invoice is already paid");

        invoice.setStatus(InvoiceStatus.PAID);
        invoice.setPaidAt(LocalDateTime.now());
        invoiceRepository.save(invoice);

        return mapToResponse(invoice);
    }

    // ======== GET ALL INVOICES FOR A SCHOOL IN A MONTH ========

    public SchoolRevenueDto getSchoolRevenueByMonth(YearMonth period, SchoolAdminProfile admin) {

        School school = admin.getSchool();

        List<Invoice> invoices =
            invoiceRepository.findAllBySchoolAndPeriod(school, period);

        BigDecimal totalExpected = invoices.stream()
            .filter(i -> i.getStatus() != InvoiceStatus.CANCELLED)
            .map(Invoice::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalCollected = invoices.stream()
            .filter(i -> i.getStatus() == InvoiceStatus.PAID)
            .map(Invoice::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalPending = invoices.stream()
            .filter(i -> i.getStatus() == InvoiceStatus.PENDING)
            .map(Invoice::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalOverdue = invoices.stream()
            .filter(i -> i.getStatus() == InvoiceStatus.OVERDUE)
            .map(Invoice::getTotalAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

        return SchoolRevenueDto.builder()
            .period(period)
            .totalExpected(totalExpected)
            .totalCollected(totalCollected)
            .totalPending(totalPending)
            .totalOverdue(totalOverdue)
            .invoiceCount(invoices.size())
            .invoices(invoices.stream().map(this::mapToResponse).toList())
            .build();
    }

    // ======== GET STUDENT'S OWN INVOICES ========

    public List<StudentInvoiceResponseDto> getMyInvoices(User currentUser) {

        StudentProfile student = studentRepository.findByUser(currentUser).orElseThrow(()->new ResourceNotFoundException("student not found with user "+currentUser.getFullName()));

        return invoiceRepository.findAllByStudentOrderByPeriodDesc(student)
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    private StudentInvoiceResponseDto mapToResponse(Invoice i) {
        return StudentInvoiceResponseDto.builder()
            .id(i.getId())
            .studentName(i.getStudent().getUser().getFullName())
            .moduleName(i.getEnrollment().getModule().getName())
            .period(i.getPeriod())
            .amount(i.getTotalAmount())
            .status(i.getStatus())
            .dueDate(i.getDueDate())
            .paidAt(i.getPaidAt())
            .build();
    }
}
	


