package com.menu.demo.Repositories;

import java.time.LocalDate;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


import com.menu.demo.Models.CourseModule;
import com.menu.demo.Models.School;
import com.menu.demo.Models.Session;


@Repository
public interface SessionRepository  extends JpaRepository <Session ,Long>{
	
	List<Session> findAllBySchoolAndArchivedFalse(School school);
	Optional<Session> findByModuleAndDateAndArchivedFalse(
		    CourseModule module,
		    LocalDate date
		);
	List<Session> findAllByModuleAndDateAfterAndArchivedFalse(
		    CourseModule module,
		    LocalDate date
		);
	 @Modifying
	    @Query("""
	        UPDATE Session s
	        SET s.archived = true
	        WHERE s.module = :module
	        AND s.date > :fromDate
	        AND s.archived = false
	    """)
	    void archiveFutureSessionsByModule(
	        @Param("module") CourseModule module,
	        @Param("fromDate") LocalDate fromDate
	    );


	List<Session> findAllByModuleOrderByDateAsc(CourseModule module);
	 // ── NEW: date range (for week view) ──────────────────────────
    @Query("""
        SELECT s FROM Session s
        WHERE s.school = :school
        AND s.date >= :start
        AND s.date <= :end
        AND s.archived = false
        ORDER BY s.date ASC, s.startTime ASC
    """)
    List<Session> findAllBySchoolAndDateRange(
        @Param("school") School school,
        @Param("start") LocalDate start,
        @Param("end") LocalDate end
    );

    // ── NEW: by teacher and date range (teacher week view) ────────
    @Query("""
        SELECT s FROM Session s
        WHERE s.module.teacher.id = :teacherId
        AND s.date >= :start
        AND s.date <= :end
        AND s.archived = false
        ORDER BY s.date ASC, s.startTime ASC
    """)
    List<Session> findByTeacherAndDateRange(
        @Param("teacherId") Long teacherId,
        @Param("start") LocalDate start,
        @Param("end") LocalDate end
    );

    // ── NEW: sessions for a module in a specific month (for billing) ──
    @Query("""
        SELECT s FROM Session s
        WHERE s.module = :module
        AND FUNCTION('YEAR',  s.date) = :year
        AND FUNCTION('MONTH', s.date) = :month
        AND s.archived = false
    """)
    List<Session> findByModuleAndYearMonth(
        @Param("module") CourseModule module,
        @Param("year") int year,
        @Param("month") int month
    );
    @Query("""
            SELECT s FROM Session s
            WHERE s.school = :school
            AND s.date = :date
            AND s.archived = false
            ORDER BY s.startTime ASC
        """)
        List<Session> findAllBySchoolAndDate(
            @Param("school") School school,
            @Param("date") LocalDate date
        );
}
