package com.menu.demo.Services;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.menu.demo.Exceptions.ResourceNotFoundException;
import com.menu.demo.Models.ClassRoom;
import com.menu.demo.Models.CourseModule;
import com.menu.demo.Models.ModuleSchedule;
import com.menu.demo.Models.School;
import com.menu.demo.Models.SchoolAdminProfile;
import com.menu.demo.Models.Session;
import com.menu.demo.Models.Subject;
import com.menu.demo.Models.TeacherProfile;
import com.menu.demo.Repositories.ClassroomRepository;
import com.menu.demo.Repositories.CourseModuleRepository;
import com.menu.demo.Repositories.EnrollmentRepository;
import com.menu.demo.Repositories.ModuleScheduleRepository;
import com.menu.demo.Repositories.SessionRepository;
import com.menu.demo.Repositories.Subjectrepository;
import com.menu.demo.Repositories.TeacherRepository;

import Dto.CourseModuleRequestDto;
import Dto.CourseModuleResponseDto;
import Dto.ScheduleEntryDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseModuleService {

    private final CourseModuleRepository moduleRepository;
    private final SessionRepository sessionRepository;
    private final TeacherRepository teacherRepository;
    private final Subjectrepository subjectRepository;
    private final ClassroomRepository classroomRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final ModuleScheduleRepository moduleScheduleRepository;

    // ============== CREATE MODULE ==============

    public CourseModuleResponseDto createCourseModule(
            CourseModuleRequestDto request,
            SchoolAdminProfile admin) {

        School school = admin.getSchool();

        TeacherProfile teacher = teacherRepository.findById(request.getTeacherId())
            .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));
        Subject subject = subjectRepository.findById(request.getSubjectId())
            .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        ClassRoom classroom = classroomRepository.findById(request.getClassroomId())
            .orElseThrow(() -> new ResourceNotFoundException("Classroom not found"));

        if (!teacher.getSchool().getId().equals(school.getId()))
            throw new AccessDeniedException("Teacher does not belong to your school");
        if (!subject.getSchool().getId().equals(school.getId()))
            throw new AccessDeniedException("Subject does not belong to your school");
        if (!classroom.getSchool().getId().equals(school.getId()))
            throw new AccessDeniedException("Classroom does not belong to your school");
        if (!teacher.getSubjects().contains(subject))
            throw new AccessDeniedException("Teacher does not teach this subject");

        if (request.getSchedules() == null || request.getSchedules().isEmpty())
            throw new IllegalArgumentException("At least one schedule day is required");

        for (ScheduleEntryDto entry : request.getSchedules()) {
            if (moduleRepository.existsTeacherScheduleConflict(
                    teacher, entry.getDay(), entry.getStartTime(), entry.getEndTime()))
                throw new IllegalStateException(
                    "Teacher already has a module on " + entry.getDay() + " at that time");
            if (moduleRepository.existsClassroomScheduleConflict(
                    classroom, entry.getDay(), entry.getStartTime(), entry.getEndTime()))
                throw new IllegalStateException(
                    "Classroom already booked on " + entry.getDay() + " at that time");
        }

        CourseModule module = CourseModule.builder()
            .school(school).subject(subject).teacher(teacher).classroom(classroom)
            .level(request.getLevel()).name(request.getName())
            .periodStart(request.getPeriodStart()).periodEnd(request.getPeriodEnd())
            .maxStudents(request.getMaxStudents()).archived(false).build();

        List<ModuleSchedule> schedules = request.getSchedules().stream()
            .map(entry -> ModuleSchedule.builder()
                .module(module).day(entry.getDay())
                .startTime(entry.getStartTime()).endTime(entry.getEndTime()).build())
            .collect(Collectors.toList());

        module.setSchedules(schedules);
        moduleRepository.save(module);
        generateSessions(module);

        return mapToResponse(module);
    }

    // ============== UPDATE MODULE ==============

    public CourseModuleResponseDto updateCourseModule(
            Long moduleId,
            CourseModuleRequestDto request,
            SchoolAdminProfile admin) {

        School school = admin.getSchool();

        CourseModule module = moduleRepository.findById(moduleId)
            .orElseThrow(() -> new ResourceNotFoundException("Module not found: " + moduleId));

        if (!module.getSchool().getId().equals(school.getId()))
            throw new AccessDeniedException("Module does not belong to your school");

        TeacherProfile teacher = teacherRepository.findById(request.getTeacherId())
            .orElseThrow(() -> new ResourceNotFoundException("Teacher not found"));
        Subject subject = subjectRepository.findById(request.getSubjectId())
            .orElseThrow(() -> new ResourceNotFoundException("Subject not found"));
        ClassRoom classroom = classroomRepository.findById(request.getClassroomId())
            .orElseThrow(() -> new ResourceNotFoundException("Classroom not found"));

        if (!teacher.getSchool().getId().equals(school.getId()))
            throw new AccessDeniedException("Teacher does not belong to your school");
        if (!subject.getSchool().getId().equals(school.getId()))
            throw new AccessDeniedException("Subject does not belong to your school");
        if (!classroom.getSchool().getId().equals(school.getId()))
            throw new AccessDeniedException("Classroom does not belong to your school");
        if (!teacher.getSubjects().contains(subject))
            throw new AccessDeniedException("Teacher does not teach this subject");

        // Update basic fields
        module.setName(request.getName());
        module.setLevel(request.getLevel());
        module.setMaxStudents(request.getMaxStudents());
        module.setSubject(subject);
        module.setTeacher(teacher);
        module.setClassroom(classroom);
        module.setPeriodStart(request.getPeriodStart());
        module.setPeriodEnd(request.getPeriodEnd());

        // Replace schedules if provided
        if (request.getSchedules() != null && !request.getSchedules().isEmpty()) {
            // delete old schedules
            moduleScheduleRepository.deleteAll(module.getSchedules());
            module.getSchedules().clear();

            // insert new schedules
            List<ModuleSchedule> newSchedules = request.getSchedules().stream()
                .map(entry -> ModuleSchedule.builder()
                    .module(module).day(entry.getDay())
                    .startTime(entry.getStartTime()).endTime(entry.getEndTime()).build())
                .collect(Collectors.toList());

            module.getSchedules().addAll(newSchedules);

            // archive all future sessions and regenerate
            sessionRepository.archiveFutureSessionsByModule(module, LocalDate.now());
            generateSessions(module);
        }

        moduleRepository.save(module);
        return mapToResponse(module);
    }

    // ============== GET ALL MODULES BY SCHOOL ==============

    public List<CourseModuleResponseDto> getModulesBySchool(SchoolAdminProfile admin) {
        return moduleRepository.findAllBySchoolAndArchivedFalse(admin.getSchool())
            .stream().map(this::mapToResponse).toList();
    }

    // ============== GET MODULES BY LEVEL ==============

    public List<CourseModuleResponseDto> getModulesBySchoolAndLevel(Long schoolId, String level) {
        School school = School.builder().id(schoolId).build();
        return moduleRepository.findAllBySchoolAndLevelAndArchivedFalse(school, level)
            .stream().map(this::mapToResponse).toList();
    }

    // ============== ARCHIVE MODULE ==============

    public void archiveModule(Long moduleId, SchoolAdminProfile admin) {
        CourseModule module = moduleRepository.findById(moduleId)
            .orElseThrow(() -> new ResourceNotFoundException("Module not found: " + moduleId));
        if (!module.getSchool().getId().equals(admin.getSchool().getId()))
            throw new AccessDeniedException("Module does not belong to your school");
        sessionRepository.archiveFutureSessionsByModule(module, LocalDate.now());
        module.setArchived(true);
        moduleRepository.save(module);
    }

    // ============== BROWSE ==============

    public List<CourseModuleResponseDto> browseModules(Long schoolId, String level) {
        return getModulesBySchoolAndLevel(schoolId, level);
    }

    // ============== SESSION GENERATION ==============

    private void generateSessions(CourseModule module) {
        Map<DayOfWeek, ModuleSchedule> scheduleMap = module.getSchedules().stream()
            .collect(Collectors.toMap(ModuleSchedule::getDay, s -> s));

        List<Session> sessions = new ArrayList<>();
        LocalDate current = module.getPeriodStart();

        while (!current.isAfter(module.getPeriodEnd())) {
            DayOfWeek day = current.getDayOfWeek();
            if (scheduleMap.containsKey(day)) {
                ModuleSchedule schedule = scheduleMap.get(day);
                sessions.add(Session.builder()
                    .module(module).school(module.getSchool()).date(current)
                    .startTime(schedule.getStartTime()).endTime(schedule.getEndTime())
                    .archived(false).build());
            }
            current = current.plusDays(1);
        }
        sessionRepository.saveAll(sessions);
    }

    // ============== MAPPING ==============

    private CourseModuleResponseDto mapToResponse(CourseModule module) {
        long enrolled = enrollmentRepository.countActiveByModule(module);

        List<ScheduleEntryDto> schedules = module.getSchedules().stream()
            .map(s -> {
                ScheduleEntryDto dto = new ScheduleEntryDto();
                dto.setDay(s.getDay());
                dto.setStartTime(s.getStartTime());
                dto.setEndTime(s.getEndTime());
                return dto;
            }).toList();

        return CourseModuleResponseDto.builder()
            .id(module.getId())
            .name(module.getName())
            .level(module.getLevel())
            .subjectId(module.getSubject().getId())
            .subjectName(module.getSubject().getName())
            .teacherId(module.getTeacher().getId())
            .teacherName(module.getTeacher().getUser().getFullName())
            .classroomId(module.getClassroom().getId())
            .classroomName(module.getClassroom().getName())
            .periodStart(module.getPeriodStart())
            .periodEnd(module.getPeriodEnd())
            .maxStudents(module.getMaxStudents())
            .enrolledCount((int) enrolled)
            .schedules(schedules)
            .archived(module.isArchived())
            .build();
    }
}