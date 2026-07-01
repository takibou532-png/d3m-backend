package com.menu.demo.Services;


import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.menu.demo.Enums.AttendenceStatus;
import com.menu.demo.Exceptions.ResourceNotFoundException;
import com.menu.demo.Models.Attendence;
import com.menu.demo.Models.CourseModule;
import com.menu.demo.Models.SchoolAdminProfile;
import com.menu.demo.Models.Session;
import com.menu.demo.Models.StudentProfile;

import com.menu.demo.Repositories.AttendanceRepository;
import com.menu.demo.Repositories.EnrollmentRepository;
import com.menu.demo.Repositories.SessionRepository;

import Dto.AttendanceEntryDto;
import Dto.AttendanceResponseDto;
import Dto.AttendanceSummaryDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class AttendenceService {

    private final AttendanceRepository attendanceRepository;
    private final SessionRepository sessionRepository;
    private final EnrollmentRepository enrollmentRepository;



    public List<AttendanceResponseDto> markAttendance(
            Long sessionId,
            List<AttendanceEntryDto> entries,
            SchoolAdminProfile admin) {

        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

  

        if (session.isArchived())
            throw new IllegalStateException("Cannot mark attendance for an archived session");

        // Get all active students in this module
        List<StudentProfile> enrolledStudents =
            enrollmentRepository.findActiveStudentsByModuleId(session.getModule().getId());

        List<Attendence> records = new ArrayList<>();

        for (AttendanceEntryDto entry : entries) {

            // Student must be enrolled in this module
            boolean isEnrolled = enrolledStudents.stream()
                .anyMatch(s -> s.getId().equals(entry.getStudentId()));

            if (!isEnrolled)
                throw new IllegalStateException(
                    "Student " + entry.getStudentId() + " is not enrolled in this module");

            // Already marked? Update it
            attendanceRepository.findByStudentIdAndSessionId(entry.getStudentId(), sessionId)
                .ifPresentOrElse(
                    existing -> {
                        existing.setStatus(entry.getStatus());
                     
                        attendanceRepository.save(existing);
                    },
                    () -> records.add(Attendence.builder()
                        .student(StudentProfile.builder().id(entry.getStudentId()).build())
                        .session(session)
                        .status(entry.getStatus())
                       
                        .build())
                );
        }

        attendanceRepository.saveAll(records);

        return attendanceRepository.findAllBySession(session)
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    // ======== GET ATTENDANCE SHEET FOR A SESSION ========

    public List<AttendanceResponseDto> getAttendanceBySession(Long sessionId) {

        Session session = sessionRepository.findById(sessionId)
            .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

        // Get all enrolled students
        List<StudentProfile> enrolled =
            enrollmentRepository.findActiveStudentsByModuleId(session.getModule().getId());

        // Get already-marked attendance
        List<Attendence> marked = attendanceRepository.findAllBySession(session);

        Map<Long, Attendence> markedMap = marked.stream()
            .collect(Collectors.toMap(a -> a.getStudent().getId(), a -> a));

        // Return all students — marked ones with status, unmarked as null
        return 
        		enrolled.stream()
            .map(student -> {
                Attendence a = markedMap.get(student.getId());
                return AttendanceResponseDto.builder()
                    .studentId(student.getId())
                 
                    .status(a != null ? a.getStatus() : null)   // null = not marked yet
               
                    .markedAt(a != null ? a.getMarkedAt() : null)
                    .build();
            })
            .toList();
    }

    // ======== GET STUDENT ATTENDANCE IN A MODULE ========

    public AttendanceSummaryDto getStudentAttendanceInModule(
            Long moduleId,
            StudentProfile student) {

        CourseModule module = CourseModule.builder().id(moduleId).build();

        List<Attendence> records =
            attendanceRepository.findByStudentAndModule(student, module);

        long present = records.stream()
            .filter(a -> a.getStatus() == AttendenceStatus.PRESENT).count();
        long absent = records.stream()
            .filter(a -> a.getStatus() == AttendenceStatus.ABSENT).count();
        
  

        return AttendanceSummaryDto.builder()
            .totalSessions(records.size())
            .present(present)
            .absent(absent)
           
            .attendanceRate(records.isEmpty() ? 0 :
                Math.round((present * 100.0) / records.size()))
            .records(records.stream().map(this::mapToResponse).toList())
            .build();
    }

    private AttendanceResponseDto mapToResponse(Attendence a) {
        return AttendanceResponseDto.builder()
            .studentId(a.getStudent().getId())
       
            .status(a.getStatus())
            
            .markedAt(a.getMarkedAt())
            .build();
    }
}
