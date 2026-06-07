package com.menu.demo.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.menu.demo.Models.ClassRoom;
import com.menu.demo.Models.School;

@Repository
public interface ClassroomRepository extends JpaRepository <ClassRoom, Long>{
	List<ClassRoom> findAllBySchool(School school);
	boolean existsByNameAndSchool(String name, School school);

}
