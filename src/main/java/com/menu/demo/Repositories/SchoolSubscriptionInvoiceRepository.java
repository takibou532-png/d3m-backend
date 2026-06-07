package com.menu.demo.Repositories;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.menu.demo.Models.School;
import com.menu.demo.Models.SchoolSubscriptionInvoice;

@Repository
public interface SchoolSubscriptionInvoiceRepository extends JpaRepository<SchoolSubscriptionInvoice, Long> {

	List<SchoolSubscriptionInvoice> findAllBySchoolOrderByYearDesc(School school);

    List<SchoolSubscriptionInvoice> findAllByYear(Integer year);

    boolean existsBySchoolAndYear(School school, Integer year);

    @Query("""
        SELECT i FROM SchoolSubscriptionInvoice i
        WHERE i.status = 'PENDING'
        AND i.dueDate < :today
    """)
    List<SchoolSubscriptionInvoice> findAllOverdue(@Param("today") LocalDate today);

    // Platform total revenue in a month
    @Query("""
            SELECT COALESCE(SUM(i.amount), 0)
            FROM SchoolSubscriptionInvoice i
            WHERE i.year = :year
            AND i.status = 'PAID'
        """)
        BigDecimal sumPaidByYear(@Param("year") Integer year);

    // Platform total revenue all time
    @Query("SELECT COALESCE(SUM(i.amount), 0) FROM SchoolSubscriptionInvoice i WHERE i.status = 'PAID'")
    BigDecimal sumAllPaid();
}
