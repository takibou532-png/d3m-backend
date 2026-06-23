package com.menu.demo.Services;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.menu.demo.Enums.EnrollmentStatus;
import com.menu.demo.Enums.Role;
import com.menu.demo.Enums.SubscriptionStatus;
import com.menu.demo.Exceptions.ResourceNotFoundException;
import com.menu.demo.Models.CourseModule;
import com.menu.demo.Models.Enrollment;
import com.menu.demo.Models.School;
import com.menu.demo.Models.SchoolAdminProfile;
import com.menu.demo.Models.Session;
import com.menu.demo.Models.StudentProfile;
import com.menu.demo.Models.StudentRequest;
import com.menu.demo.Models.User;
import com.menu.demo.Repositories.CourseModuleRepository;
import com.menu.demo.Repositories.EnrollmentRepository;

import com.menu.demo.Repositories.SessionRepository;
import com.menu.demo.Repositories.StudentRepository;
import com.menu.demo.Repositories.StudentRequestRepository;

import Dto.EnrollmentResponseDto;
import Dto.StudentEnrollmentRequestDto;
import Dto.StudentRequestResponseDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final StudentRepository studentProfileRepository;
    private final CourseModuleRepository moduleRepository;
    private final StudentRequestRepository studentRequestRepository;

    // ===== STUDENT SENDS ENROLLMENT REQUEST =====

    public void requestEnrollment(StudentEnrollmentRequestDto request, User currentUser) {

        StudentProfile student = studentProfileRepository.findByUser(currentUser)
            .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        CourseModule module = moduleRepository.findById(request.getModuleId())
            .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        if (module.isArchived())
            throw new IllegalStateException("This module is no longer available");

        // Already requested or enrolled?
        if (studentRequestRepository.existsByStudentAndModuleAndStatus(
                student, module, EnrollmentStatus.PENDING))
            throw new IllegalStateException("You already have a pending request for this module");

        // FIX: check for ACTIVE (not ACCEPTED) since approved enrollments are saved as ACTIVE
        if (enrollmentRepository.findByStudentAndModule(student, module)
                .filter(e -> e.getStatus() == EnrollmentStatus.ACTIVE)
                .isPresent())
            throw new IllegalStateException("You are already enrolled in this module");

        // Check module capacity
        long enrolled = enrollmentRepository.countActiveByModule(module);
        if (enrolled >= module.getMaxStudents())
            throw new IllegalStateException("This module is full");

        StudentRequest studentRequest = StudentRequest.builder()
            .student(student)
            .module(module)
            .status(EnrollmentStatus.PENDING)
            .build();

        studentRequestRepository.save(studentRequest);
    }

    // ===== SCHOOL ADMIN APPROVES REQUEST =====

    public void approveEnrollment(Long requestId, SchoolAdminProfile admin) throws AccessDeniedException {

        StudentRequest request = studentRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (!request.getModule().getSchool().getId().equals(admin.getSchool().getId()))
            throw new AccessDeniedException("Not your school");

        if (request.getStatus() != EnrollmentStatus.PENDING)
            throw new IllegalStateException("Request is not pending");

        // Final capacity check
        long enrolled = enrollmentRepository.countActiveByModule(request.getModule());
        if (enrolled >= request.getModule().getMaxStudents())
            throw new IllegalStateException("Module is full");

        // FIX: save enrollment as ACTIVE so findActiveStudentsByModuleId can find it
        Enrollment enrollment = Enrollment.builder()
            .student(request.getStudent())
            .module(request.getModule())
            .status(EnrollmentStatus.ACTIVE)
            .startDate(LocalDate.now())
            .endDate(request.getModule().getPeriodEnd())
            .monthlyPrice(request.getModule().getMonthlyprice())
            .build();

        enrollmentRepository.save(enrollment);

        // Update request status — this is the StudentRequest row, stays ACCEPTED
        request.setStatus(EnrollmentStatus.ACCEPTED);
        request.setReviewedAt(LocalDateTime.now());
        studentRequestRepository.save(request);
    }

    // ===== SCHOOL ADMIN REJECTS REQUEST =====

    public void rejectEnrollment(Long requestId, String comment, SchoolAdminProfile admin) throws AccessDeniedException {

        StudentRequest request = studentRequestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Request not found"));

        if (!request.getModule().getSchool().getId().equals(admin.getSchool().getId()))
            throw new AccessDeniedException("Not your school");

        request.setStatus(EnrollmentStatus.REJECTED);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewComment(comment);
        studentRequestRepository.save(request);
    }

    // ===== STUDENT VIEWS THEIR OWN ENROLLMENTS =====

    public List<EnrollmentResponseDto> getMyEnrollments(User currentUser) {

        StudentProfile student = studentProfileRepository.findByUser(currentUser)
            .orElseThrow(() -> new ResourceNotFoundException("Student profile not found"));

        // FIX: query ACTIVE (not ACCEPTED) to match what approveEnrollment saves
        return enrollmentRepository
            .findAllByStudentAndStatus(student, EnrollmentStatus.ACTIVE)
            .stream()
            .map(this::mapToResponse)
            .toList();
    }

    // ===== SCHOOL ADMIN — view all requests (filterable by status) =====

    public Page<StudentRequestResponseDto> getSchoolRequests(
            EnrollmentStatus status,
            Pageable pageable,
            SchoolAdminProfile admin) {

        School school = admin.getSchool();

        Page<StudentRequest> requests = (status != null)
            ? studentRequestRepository.findBySchoolAndStatus(school, status, pageable)
            : studentRequestRepository.findAllBySchool(school, pageable);

        return requests.map(this::mapRequestToResponse);
    }

    // ===== SCHOOL ADMIN — view requests for a specific module =====

    public List<StudentRequestResponseDto> getRequestsByModule(
            Long moduleId,
            EnrollmentStatus status,
            SchoolAdminProfile admin) {

        CourseModule module = moduleRepository.findById(moduleId)
            .orElseThrow(() -> new ResourceNotFoundException("Module not found: " + moduleId));

        if (!module.getSchool().getId().equals(admin.getSchool().getId()))
            throw new AccessDeniedException("Module does not belong to your school");

        List<StudentRequest> requests = (status != null)
            ? studentRequestRepository.findByModuleAndStatus(module, status)
            : studentRequestRepository.findByModuleAndStatus(module, EnrollmentStatus.PENDING);

        return requests.stream().map(this::mapRequestToResponse).toList();
    }

    // ===== SCHOOL ADMIN — count pending requests (dashboard badge) =====

    public long countPendingRequests(SchoolAdminProfile admin) {
        return studentRequestRepository.countPendingBySchool(admin.getSchool());
    }

    // ===== MAPPING =====

    private StudentRequestResponseDto mapRequestToResponse(StudentRequest sr) {
        StudentProfile student = sr.getStudent();
        CourseModule module = sr.getModule();

        return StudentRequestResponseDto.builder()
            .id(sr.getId())
            // student
            .studentId(student.getId())
            .studentFullName(student.getUser().getFullName())
            .studentEmail(student.getUser().getEmail())
            .studentLevel(student.getLevel())
            .parentName(student.getParentName())
            .parentPhone(student.getParentPhone())
            // module
            .moduleId(module.getId())
            .moduleName(module.getName())
            .subjectName(module.getSubject().getName())
            .level(module.getLevel())
            .monthlyPrice(module.getMonthlyprice())
            // request
            .status(sr.getStatus())
            .createdAt(sr.getCreatedAt())
            .reviewedAt(sr.getReviewedAt())
            .reviewComment(sr.getReviewComment())
            .build();
    }

    // ===== STUDENT CANCEL =====

    public void cancelEnrollment(Long enrollmentId, User studentUser) throws AccessDeniedException {

        if (studentUser.getRole() != Role.STUDENT)
            throw new AccessDeniedException("Only student");

        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
            .orElseThrow(() -> new ResourceNotFoundException("Enrollment not found"));

        if (!enrollment.getStudent().getUser().getId().equals(studentUser.getId()))
            throw new AccessDeniedException("Not your enrollment");

        if (!enrollment.getStatus().equals(EnrollmentStatus.PENDING))
            throw new IllegalStateException("Cannot cancel approved enrollment");

        enrollment.setStatus(EnrollmentStatus.CANCELLED);
    }

    private EnrollmentResponseDto mapToResponse(Enrollment e) {
        return EnrollmentResponseDto.builder()
            .id(e.getId())
            .ModuleId(e.getModule().getId())
            .moduleName(e.getModule().getName())
            .subjectName(e.getModule().getSubject().getName())
            .teacherName(e.getModule().getTeacher().getUser().getFullName())
            .status(e.getStatus())
            .startDate(e.getStartDate())
            .endDate(e.getEndDate())
            .student(e.getStudent())
            .monthlyPrice(e.getMonthlyPrice())
            .build();
    }
}