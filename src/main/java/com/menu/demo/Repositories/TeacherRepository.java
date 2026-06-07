package com.menu.demo.Repositories;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.menu.demo.Models.School;

import com.menu.demo.Models.TeacherProfile;
import com.menu.demo.Models.User;
@Repository
public interface TeacherRepository extends JpaRepository<TeacherProfile, Long>{
	  Optional<TeacherProfile> findByUser(User user);
      List<TeacherProfile> findAllBySchool(School school);
      long countBySchool(School school);
}
