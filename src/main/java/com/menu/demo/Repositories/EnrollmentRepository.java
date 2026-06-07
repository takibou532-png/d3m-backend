package com.menu.demo.Repositories;


import java.util.List;
import java.util.Optional;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.menu.demo.Enums.EnrollmentStatus;
import com.menu.demo.Models.CourseModule;
import com.menu.demo.Models.Enrollment;
import com.menu.demo.Models.School;

import com.menu.demo.Models.StudentProfile;

@Repository
public interface EnrollmentRepository extends JpaRepository < Enrollment , Long>{
 

	List<Enrollment> findAllByStatus(EnrollmentStatus status);
 List<Enrollment> findAllByStudent(StudentProfile student);

 @Query("SELECT COUNT(DISTINCT e.student) FROM Enrollment e WHERE e.module.school = :school AND e.status = 'ACTIVE'")
 long countDistinctStudentsBySchool(@Param("school") School school);
 boolean existsByStudentAndModule(StudentProfile student, CourseModule module);

 







 List<Enrollment> findAllByStudentAndStatus(StudentProfile student ,EnrollmentStatus status);
 List<Enrollment> findAllByModuleAndStatus(CourseModule module, EnrollmentStatus status);

 Optional<Enrollment> findByStudentAndModule(StudentProfile student, CourseModule module);

 @Query("SELECT COUNT(e) FROM Enrollment e WHERE e.module = :module AND e.status = 'ACTIVE'")
 long countActiveByModule(@Param("module") CourseModule module);

 // Get all students in a module (for attendance sheet)
 @Query("SELECT e.student FROM Enrollment e WHERE e.module.id = :moduleId AND e.status = 'ACTIVE'")
 List<StudentProfile> findActiveStudentsByModuleId(@Param("moduleId") Long moduleId);








}
