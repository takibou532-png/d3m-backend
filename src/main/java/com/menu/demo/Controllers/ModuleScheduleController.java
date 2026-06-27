package com.menu.demo.Controllers;

import java.time.DayOfWeek;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.menu.demo.Exceptions.ResourceNotFoundException;
import com.menu.demo.Models.CourseModule;
import com.menu.demo.Models.ModuleSchedule;
import com.menu.demo.Models.SchoolAdminProfile;
import com.menu.demo.Models.User;
import com.menu.demo.Repositories.CourseModuleRepository;
import com.menu.demo.Repositories.ModuleScheduleRepository;
import com.menu.demo.Repositories.SchoolAdminProfileRepository;

import Dto.ModuleScheduleUpdateDto;
import Dto.ScheduleEntryDto;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/modules")
@RequiredArgsConstructor
public class ModuleScheduleController {

    private final CourseModuleRepository moduleRepository;
    private final ModuleScheduleRepository moduleScheduleRepository;
    private final SchoolAdminProfileRepository adminProfileRepository;

    /**
     * PUT /api/modules/{moduleId}/schedules/{day}
     * day must be uppercase: SUNDAY, MONDAY, FRIDAY, etc.
     */
    @PutMapping("/{moduleId}/schedules/{day}")
    public ResponseEntity<ScheduleEntryDto> updateSchedule(
            @PathVariable Long moduleId,
            @PathVariable String day,          // receive as String to avoid enum binding issues
            @RequestBody ModuleScheduleUpdateDto dto,
            @AuthenticationPrincipal User currentUser) {

        SchoolAdminProfile admin = adminProfileRepository.findByUser(currentUser)
            .orElseThrow(() -> new ResourceNotFoundException("Admin profile not found"));

        CourseModule module = moduleRepository.findById(moduleId)
            .orElseThrow(() -> new ResourceNotFoundException("Module not found: " + moduleId));

        if (!module.getSchool().getId().equals(admin.getSchool().getId()))
            throw new AccessDeniedException("Module does not belong to your school");

        // Parse day string → DayOfWeek safely
        DayOfWeek dayOfWeek;
        try {
            dayOfWeek = DayOfWeek.valueOf(day.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid day: " + day);
        }

        ModuleSchedule schedule = moduleScheduleRepository
            .findByModuleAndDay(module, dayOfWeek)
            .orElseThrow(() -> new ResourceNotFoundException(
                "No schedule found for module " + moduleId + " on " + day));

        schedule.setStartTime(dto.getStartTime());
        schedule.setEndTime(dto.getEndTime());
        moduleScheduleRepository.save(schedule);

        ScheduleEntryDto response = new ScheduleEntryDto();
        response.setDay(schedule.getDay());
        response.setStartTime(schedule.getStartTime());
        response.setEndTime(schedule.getEndTime());

        return ResponseEntity.ok(response);
    }
}