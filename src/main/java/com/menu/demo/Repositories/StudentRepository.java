package com.menu.demo.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.menu.demo.Models.School;
import com.menu.demo.Models.StudentProfile;
import com.menu.demo.Models.User;
@Repository
public interface StudentRepository extends JpaRepository <StudentProfile,Long>{
        Optional<StudentProfile> findByUser(User user);
      
        
}
