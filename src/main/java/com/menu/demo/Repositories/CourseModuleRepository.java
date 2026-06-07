package com.menu.demo.Repositories;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.menu.demo.Models.ClassRoom;
import com.menu.demo.Models.CourseModule;
import com.menu.demo.Models.School;
import com.menu.demo.Models.TeacherProfile;


@Repository
public interface CourseModuleRepository extends JpaRepository <CourseModule , Long>{
	   List<CourseModule> findAllBySchoolAndArchivedFalse(School school);
       Optional<CourseModule> findByName(String name);
	    List<CourseModule> findAllBySchoolAndLevelAndArchivedFalse(School school, String level);

	    // Check teacher conflict across modules on same day/time
	    @Query("""
	        SELECT COUNT(ms) > 0
	        FROM ModuleSchedule ms
	        WHERE ms.module.teacher = :teacher
	        AND ms.day = :day
	        AND ms.module.archived = false
	        AND ms.startTime < :endTime
	        AND ms.endTime > :startTime
	    """)
	    boolean existsTeacherScheduleConflict(
	        @Param("teacher") TeacherProfile teacher,
	        @Param("day") DayOfWeek day,
	        @Param("startTime") LocalTime startTime,
	        @Param("endTime") LocalTime endTime
	    );

	    @Query("""
	        SELECT COUNT(ms) > 0
	        FROM ModuleSchedule ms
	        WHERE ms.module.classroom = :classroom
	        AND ms.day = :day
	        AND ms.module.archived = false
	        AND ms.startTime < :endTime
	        AND ms.endTime > :startTime
	    """)
	    boolean existsClassroomScheduleConflict(
	        @Param("classroom") ClassRoom classroom,
	        @Param("day") DayOfWeek day,
	        @Param("startTime") LocalTime startTime,
	        @Param("endTime") LocalTime endTime
	    );

}
