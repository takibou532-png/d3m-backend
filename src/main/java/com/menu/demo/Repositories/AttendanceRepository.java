package com.menu.demo.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.menu.demo.Models.Attendence;
import com.menu.demo.Models.CourseModule;
import com.menu.demo.Models.Session;
import com.menu.demo.Models.StudentProfile;
@Repository
public interface AttendanceRepository extends JpaRepository<Attendence, Long> {

	// All attendance records for a session (teacher marks attendance)
    List<Attendence> findAllBySession(Session session);
    Optional<Attendence> findByStudentIdAndSessionId(Long studentId, Long sessionId);
    // Check if already marked
    boolean existsByStudentAndSession(StudentProfile student, Session session);

    // Student's attendance in a module
    @Query("""
        SELECT a FROM Attendence a
        WHERE a.student = :student
        AND a.session.module = :module
        ORDER BY a.session.date ASC
    """)
    List<Attendence> findByStudentAndModule(
        @Param("student") StudentProfile student,
        @Param("module") CourseModule module
    );
    // Count present sessions for a student in a month (for stats)
    @Query("""
        SELECT COUNT(a) FROM Attendence a
        WHERE a.student = :student
        AND a.session.module = :module
        AND a.status = 'PRESENT'
        AND FUNCTION('YEAR', a.session.date) = :year
        AND FUNCTION('MONTH', a.session.date) = :month
    """)
    long countPresentByStudentAndModuleAndMonth(
        @Param("student") StudentProfile student,
        @Param("module") CourseModule module,
        @Param("year") int year,
        @Param("month") int month
    );

    // All sessions in a month for a module (total scheduled)
    @Query("""
        SELECT COUNT(a) FROM Attendence a
        WHERE a.session.module = :module
        AND FUNCTION('YEAR', a.session.date) = :year
        AND FUNCTION('MONTH', a.session.date) = :month
    """)
    long countTotalByModuleAndMonth(
        @Param("module") CourseModule module,
        @Param("year") int year,
        @Param("month") int month
    );


    Optional<Attendence> findByStudentAndSession(StudentProfile student, Session session);

   

    // Any attendance marked for this session?
    boolean existsBySession(Session session);

 
 


 

}
