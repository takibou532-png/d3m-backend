package com.menu.demo.Repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.menu.demo.Enums.InvoiceStatus;
import com.menu.demo.Models.Enrollment;
import com.menu.demo.Models.Invoice;
import com.menu.demo.Models.School;
import com.menu.demo.Models.StudentProfile;
@Repository
public interface InvoiceRepository extends JpaRepository<Invoice, Long> {

    List<Invoice> findByStudent(StudentProfile student);
    boolean existsByStudentAndPeriodAndEnrollment(StudentProfile student,YearMonth period,Enrollment enrollment );

    Optional<Invoice> findByStudentAndPeriod(StudentProfile student, YearMonth period);

    List<Invoice> findAllBySchoolAndPeriod(School school, YearMonth period);

    Optional<Invoice> findFirstByStudentAndStatusOrderByPeriodDesc(StudentProfile student, InvoiceStatus status);

    List<Invoice> findByStatusAndDueDateBefore(InvoiceStatus status, LocalDate date);
    // All invoices for a student
    List<Invoice> findAllByStudentOrderByPeriodDesc(StudentProfile student);

   

    // All invoices for a school (for revenue dashboard)
    List<Invoice> findAllBySchool(School school);

    // Avoid duplicate invoice generation
    boolean existsByEnrollmentAndPeriod(Enrollment enrollment, YearMonth period);

    // All overdue invoices (for scheduler to update status)
    @Query("""
        SELECT i FROM Invoice i
        WHERE i.status = 'PENDING'
        AND i.dueDate < :today
    """)
    List<Invoice> findAllOverdue(@Param("today") LocalDate today);

    // Total revenue for a school in a period
    @Query("""
        SELECT COALESCE(SUM(i.totalAmount), 0)
        FROM Invoice i
        WHERE i.school = :school
        AND i.period = :period
        AND i.status = 'PAID'
    """)
    BigDecimal sumPaidBySchoolAndPeriod(
        @Param("school") School school,
        @Param("period") YearMonth period
    );

    // Total revenue for a school all time
    @Query("""
        SELECT COALESCE(SUM(i.totalAmount), 0)
        FROM Invoice i
        WHERE i.school = :school
        AND i.status = 'PAID'
    """)
    BigDecimal sumAllPaidBySchool(@Param("school") School school);

}
