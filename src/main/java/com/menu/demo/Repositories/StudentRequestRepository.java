package com.menu.demo.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
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
}