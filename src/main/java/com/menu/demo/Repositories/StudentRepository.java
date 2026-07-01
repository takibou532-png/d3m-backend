package com.menu.demo.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.menu.demo.Models.School;
import com.menu.demo.Models.StudentProfile;
import com.menu.demo.Models.User;
@Repository
public interface StudentRepository extends JpaRepository <StudentProfile,Long>{
        Optional<StudentProfile> findByUser(User user);
        @Query("""
    SELECT sp FROM StudentProfile sp
    WHERE sp.id IN (
        SELECT e.student.id FROM Enrollment e
        WHERE e.module.school = :school
        AND e.status = 'ACCEPTED'
    )
    AND sp.archived = false
""")
List<StudentProfile> findAllBySchool(@Param("school") School school);
      
        
}
