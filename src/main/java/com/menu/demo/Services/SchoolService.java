package com.menu.demo.Services;


import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import org.springframework.stereotype.Service;

import com.menu.demo.Enums.AdminActionType;
import com.menu.demo.Enums.SubscriptionStatus;
import com.menu.demo.Exceptions.ResourceNotFoundException;
import com.menu.demo.Models.School;
import com.menu.demo.Models.SchoolAdminProfile;
import com.menu.demo.Models.SuperAdminAction;
import com.menu.demo.Models.User;
import com.menu.demo.Repositories.EnrollmentRepository;
import com.menu.demo.Repositories.InvoiceRepository;
import com.menu.demo.Repositories.SchoolAdminProfileRepository;
import com.menu.demo.Repositories.SchoolRepository;
import com.menu.demo.Repositories.SuperAdminActionRepository;
import com.menu.demo.Repositories.TeacherRepository;
import com.menu.demo.Repositories.UserRepository;


import Dto.SchoolResponseDto;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SchoolService {

    private final SchoolRepository schoolRepository;
    private final SchoolAdminProfileRepository adminProfileRepository;
    private final UserRepository userRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final TeacherRepository teacherRepository;
    private final InvoiceRepository studentInvoiceRepository;
    private final SuperAdminActionRepository actionRepository;

    // ======== GET ALL SCHOOLS ========

    public Page<SchoolResponseDto> getAllSchools(Pageable pageable) {
        return schoolRepository
            .findAllBySubscriptionStatusNot(SubscriptionStatus.EXPIRED, pageable)
            .map(this::mapSchoolToResponse);
    }
    
    public List<SchoolResponseDto> getAllActiveSchools() {
        return schoolRepository
            .findAllBySubscriptionStatus(SubscriptionStatus.ACTIVE).stream()
            .map(this::mapSchoolToResponse)
            .toList();
    }
    
//    ================GET SCHOOL BY ADMIN
    public SchoolResponseDto getSchoolByAdmin(SchoolAdminProfile admin) {
    	
    School school=schoolRepository.findSchoolBySchoolAdminProfile(admin) .orElseThrow(() -> new ResourceNotFoundException("no school is linked to that user"));
    return mapSchoolToResponse(school);
    }


    // ======== GET ONE SCHOOL ========

    public SchoolResponseDto getSchoolById(Long schoolId) {
        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("School not found: " + schoolId));
        return mapSchoolToResponse(school);
    }

    // ======== SUSPEND SCHOOL ========

    public void suspendSchool(Long schoolId, String comment, User superAdmin) {

        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("School not found: " + schoolId));

        if (school.getSubscriptionStatus() == SubscriptionStatus.SUSPENDED)
            throw new IllegalStateException("School is already suspended");

        // Disable school admin login
        adminProfileRepository.findBySchool(school)
            .ifPresent(profile -> {
                profile.getUser().setEnabled(false);
                userRepository.save(profile.getUser());
            });

        school.setSubscriptionStatus(SubscriptionStatus.SUSPENDED);
        schoolRepository.save(school);

        logAction(superAdmin, AdminActionType.SUSPEND_SCHOOL,
            schoolId, "School", comment);
    }

    // ======== REACTIVATE SCHOOL ========

    public void reactivateSchool(Long schoolId, String comment, User superAdmin) {

        School school = schoolRepository.findById(schoolId)
            .orElseThrow(() -> new ResourceNotFoundException("School not found: " + schoolId));

        if (school.getSubscriptionStatus() != SubscriptionStatus.SUSPENDED)
            throw new IllegalStateException("School is not suspended");

        // Re-enable school admin login
        adminProfileRepository.findBySchool(school)
            .ifPresent(profile -> {
                profile.getUser().setEnabled(true);
                userRepository.save(profile.getUser());
            });

        school.setSubscriptionStatus(SubscriptionStatus.ACTIVE);
        school.setSubscriptionExpiresAt(LocalDate.now().plusMonths(1));
        schoolRepository.save(school);

        logAction(superAdmin, AdminActionType.REACTIVATE_SCHOOL,
            schoolId, "School", comment);
    }



    // ======== MAP WITH LIVE STATS ========

    private SchoolResponseDto mapSchoolToResponse(School school) {

        long totalStudents = enrollmentRepository
            .countDistinctStudentsBySchool(school);

        long totalTeachers = teacherRepository
            .countBySchool(school);

        BigDecimal currentMonthRevenue = studentInvoiceRepository
            .sumPaidBySchoolAndPeriod(school, YearMonth.now());

        return SchoolResponseDto.builder()
            .id(school.getId())
            .schoolName(school.getSchoolName())
            .ownerName(school.getOwnerName())
            .email(school.getEmail())
            .wilaya(school.getWilaya())
            .subscriptionStatus(school.getSubscriptionStatus())
          
            .subscriptionExpiresAt(school.getSubscriptionExpiresAt())
            .createdAt(school.getCreatedAt())
            .totalStudents(totalStudents)
            .totalTeachers(totalTeachers)
            .currentMonthRevenue(currentMonthRevenue)
            .build();
    }

    private void logAction(User admin, AdminActionType type,
                           Long entityId, String entityType, String comment) {
        actionRepository.save(SuperAdminAction.builder()
            .superAdmin(admin)
            .actionType(type)
            .targetEntityId(entityId)
            .targetEntityType(entityType)
            .comment(comment)
            .build());
    }
}