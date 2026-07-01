package com.menu.demo.Repositories;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.menu.demo.Models.School;
import com.menu.demo.Models.TeacherPayout;
import com.menu.demo.Models.TeacherProfile;

public interface TeacherPayoutRepository extends JpaRepository<TeacherPayout, Long> {

    List<TeacherPayout> findAllBySchoolAndPeriod(School school, YearMonth period);

    List<TeacherPayout> findAllByTeacherOrderByPeriodDesc(TeacherProfile teacher);

    // Used by getLatestPayoutForTeacher — fetches only the most recent 1
    List<TeacherPayout> findByTeacherOrderByPeriodDesc(TeacherProfile teacher, Pageable pageable);

    boolean existsByTeacherAndPeriod(TeacherProfile teacher, YearMonth period);

    Optional<TeacherPayout> findByTeacherAndPeriod(TeacherProfile teacher, YearMonth period);

    @Query("""
        SELECT COALESCE(SUM(p.payoutAmount), 0)
        FROM TeacherPayout p
        WHERE p.school = :school
        AND p.period = :period
        AND p.status = 'PENDING'
    """)
    BigDecimal sumPendingBySchoolAndPeriod(
        @Param("school") School school,
        @Param("period") YearMonth period
    );

    @Query("""
        SELECT COALESCE(SUM(p.payoutAmount), 0)
        FROM TeacherPayout p
        WHERE p.school = :school
        AND p.period = :period
        AND p.status = 'PAID'
    """)
    BigDecimal sumPaidBySchoolAndPeriod(
        @Param("school") School school,
        @Param("period") YearMonth period
    );
}