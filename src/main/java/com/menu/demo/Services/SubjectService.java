package com.menu.demo.Services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.menu.demo.Exceptions.ResourceNotFoundException;
import com.menu.demo.Models.School;
import com.menu.demo.Models.SchoolAdminProfile;
import com.menu.demo.Models.Subject;
import com.menu.demo.Models.TeacherProfile;
import com.menu.demo.Repositories.Subjectrepository;
import com.menu.demo.Repositories.TeacherRepository;

import Dto.SubjectResponseDto;
import Dto.SubjectrequestDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SubjectService {

    private final Subjectrepository subjectRepository;
    private final TeacherRepository teacherRepository;

    // ============================= CREATE SUBJECT ====================

    public ResponseEntity<SubjectResponseDto> createSubject(SubjectrequestDto request, SchoolAdminProfile admin) {

        School school = admin.getSchool();

        // Resolve teacher if provided
        TeacherProfile teacher = null;
        if (request.getTeacherId() != null) {
            teacher = teacherRepository.findById(request.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found: " + request.getTeacherId()));

            if (!teacher.getSchool().getId().equals(school.getId()))
                throw new AccessDeniedException("Teacher does not belong to your school");
        }

        Subject subject = Subject.builder()
            .name(request.getName())
            .description(request.getDescription())
            .school(school)
            .teacher(teacher)
            .isArchived(false)
            .build();

        subjectRepository.save(subject);

        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(subject));
    }

    // ====================== GET SUBJECTS BY SCHOOL ====================

    public List<SubjectResponseDto> getAllSubjects(SchoolAdminProfile admin) {
        School school = admin.getSchool();
        return subjectRepository.findAllBySchoolAndIsArchivedFalse(school)
            .stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    // =================  UPDATE SUBJECT ===========================

    public SubjectResponseDto updateSubject(Long id, SchoolAdminProfile admin, SubjectrequestDto dto) {

        Subject subject = subjectRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + id));

        if (!subject.getSchool().getId().equals(admin.getSchool().getId()))
            throw new AccessDeniedException("Not your school");

        subject.setName(dto.getName());
        subject.setDescription(dto.getDescription());

        // Update teacher assignment
        if (dto.getTeacherId() != null) {
            TeacherProfile teacher = teacherRepository.findById(dto.getTeacherId())
                .orElseThrow(() -> new ResourceNotFoundException("Teacher not found: " + dto.getTeacherId()));

            if (!teacher.getSchool().getId().equals(admin.getSchool().getId()))
                throw new AccessDeniedException("Teacher does not belong to your school");

            subject.setTeacher(teacher);
        } else {
            subject.setTeacher(null); // allow unassigning
        }

        subjectRepository.save(subject);
        return mapToResponse(subject);
    }

    // ====================== ARCHIVE SUBJECT =============================

    public ResponseEntity<SubjectResponseDto> archiveSubject(Long id, SchoolAdminProfile schoolAdmin) {

        Subject subject = subjectRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Subject not found: " + id));

        if (!subject.getSchool().getId().equals(schoolAdmin.getSchool().getId()))
            throw new AccessDeniedException("Not your school");

        subject.setArchived(true);
        subjectRepository.save(subject);

        return ResponseEntity.ok(mapToResponse(subject));
    }

    // ====================== MAPPING =============================

    private SubjectResponseDto mapToResponse(Subject subject) {
        return SubjectResponseDto.builder()
            .id(subject.getId())
            .name(subject.getName())
            .description(subject.getDescription())
            .schoolId(subject.getSchool().getId())
            .isArchived(subject.isArchived())
            .teacherId(subject.getTeacher() != null ? subject.getTeacher().getId() : null)
            .teacherName(subject.getTeacher() != null ? subject.getTeacher().getUser().getFullName() : null)
            .build();
    }
}