package com.menu.demo.Repositories;

import java.time.DayOfWeek;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.menu.demo.Models.CourseModule;
import com.menu.demo.Models.ModuleSchedule;

@Repository
public interface ModuleScheduleRepository extends JpaRepository<ModuleSchedule, Long> {

    Optional<ModuleSchedule> findByModuleAndDay(CourseModule module, DayOfWeek day);
}