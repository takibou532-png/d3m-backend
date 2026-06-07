package com.menu.demo.Repositories;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.menu.demo.Models.ClassRoom;
import com.menu.demo.Models.CourseModule;
import com.menu.demo.Models.School;
import com.menu.demo.Models.Session;
import com.menu.demo.Models.TeacherProfile;

@Repository
public interface SessionRepository  extends JpaRepository <Session ,Long>{
	
	List<Session> findAllBySchoolAndArchivedFalse(School school);
	Optional<Session> findByModuleAndDateAndArchivedFalse(
		    CourseModule module,
		    LocalDate date
		);
	List<Session> findAllByModuleAndDateAfterAndArchivedFalse(
		    CourseModule module,
		    LocalDate date
		);
	 @Modifying
	    @Query("""
	        UPDATE Session s
	        SET s.archived = true
	        WHERE s.module = :module
	        AND s.date > :fromDate
	        AND s.archived = false
	    """)
	    void archiveFutureSessionsByModule(
	        @Param("module") CourseModule module,
	        @Param("fromDate") LocalDate fromDate
	    );


	List<Session> findAllByModuleOrderByDateAsc(CourseModule module);

}
