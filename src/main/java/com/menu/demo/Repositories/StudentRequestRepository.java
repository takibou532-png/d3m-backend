package com.menu.demo.Repositories;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.menu.demo.Enums.EnrollmentStatus;
import com.menu.demo.Models.CourseModule;
import com.menu.demo.Models.School;
import com.menu.demo.Models.StudentProfile;
import com.menu.demo.Models.StudentRequest;

@Repository
public interface StudentRequestRepository extends JpaRepository<StudentRequest, Long> {

    List<StudentRequest> findAllByModuleAndStatus(CourseModule module, EnrollmentStatus status);

    boolean existsByStudentAndModuleAndStatus(
        StudentProfile student,
        CourseModule module,
        EnrollmentStatus status
    );
    // All requests for a school filtered by status (paginated)
    @Query("""
        SELECT sr FROM StudentRequest sr
        WHERE sr.module.school = :school
        AND sr.status = :status
        ORDER BY sr.createdAt DESC
    """)
    Page<StudentRequest> findBySchoolAndStatus(
        @Param("school") School school,
        @Param("status") EnrollmentStatus status,
        Pageable pageable
    );

    // All requests for a school regardless of status
    @Query("""
        SELECT sr FROM StudentRequest sr
        WHERE sr.module.school = :school
        ORDER BY sr.createdAt DESC
    """)
    Page<StudentRequest> findAllBySchool(
        @Param("school") School school,
        Pageable pageable
    );

    // All requests for a specific module
    @Query("""
        SELECT sr FROM StudentRequest sr
        WHERE sr.module = :module
        AND sr.status = :status
        ORDER BY sr.createdAt DESC
    """)
    List<StudentRequest> findByModuleAndStatus(
        @Param("module") CourseModule module,
        @Param("status") EnrollmentStatus status
    );

    // Count pending requests for a school (for dashboard badge)
    @Query("""
        SELECT COUNT(sr) FROM StudentRequest sr
        WHERE sr.module.school = :school
        AND sr.status = 'PENDING'
    """)
    long countPendingBySchool(@Param("school") School school);


}