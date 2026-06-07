package com.menu.demo.Repositories;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.menu.demo.Models.School;
import com.menu.demo.Models.SchoolAdminProfile;
import com.menu.demo.Models.User;
@Repository
public interface SchoolAdminProfileRepository extends JpaRepository<SchoolAdminProfile,Long>{
	Optional<SchoolAdminProfile> findBySchool(School school);
	Optional<SchoolAdminProfile> findByUser(User user);
}
