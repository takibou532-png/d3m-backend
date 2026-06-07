package com.menu.demo.Repositories;

import java.util.List;



import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.menu.demo.Models.School;
import com.menu.demo.Models.Subject;



@Repository
public interface Subjectrepository  extends JpaRepository<Subject, Long>{
	List<Subject> findAllBySchoolAndIsArchivedFalse(School school);

}
