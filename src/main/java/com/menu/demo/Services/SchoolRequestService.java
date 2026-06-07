package com.menu.demo.Services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.menu.demo.Enums.AdminActionType;
import com.menu.demo.Enums.EnrollmentStatus;
import com.menu.demo.Enums.RequestStatus;
import com.menu.demo.Enums.Role;
import com.menu.demo.Enums.SubscriptionStatus;
import com.menu.demo.Exceptions.ResourceNotFoundException;
import com.menu.demo.Models.School;
import com.menu.demo.Models.SchoolAdminProfile;
import com.menu.demo.Models.SchoolRequest;
import com.menu.demo.Models.SuperAdminAction;
import com.menu.demo.Models.User;
import com.menu.demo.Repositories.EnrollmentRepository;
import com.menu.demo.Repositories.InvoiceRepository;
import com.menu.demo.Repositories.SchoolAdminProfileRepository;
import com.menu.demo.Repositories.SchoolRepository;
import com.menu.demo.Repositories.SchoolRequestRepository;
import com.menu.demo.Repositories.SuperAdminActionRepository;
import com.menu.demo.Repositories.TeacherRepository;
import com.menu.demo.Repositories.UserRepository;

import Dto.SchoolRegistrationRequestDto;
import Dto.SchoolRequestResponseDto;
import Dto.SchoolResponseDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;



@Service
@RequiredArgsConstructor
@Transactional
public class SchoolRequestService {

    private final SchoolRequestRepository requestRepository;
    private final SchoolRepository schoolRepository;
    private final UserRepository userRepository;
    private final SchoolAdminProfileRepository adminProfileRepository;
    private final SuperAdminActionRepository actionRepository;
    private final PasswordEncoder passwordEncoder;
    private final EnrollmentRepository enrollmentRepository;
    private final InvoiceRepository studentInvoiceRepository;
    private final TeacherRepository teacherRepository;

    // ======== PUBLIC — school owner submits registration ========

    public SchoolRequestResponseDto submitRequest(SchoolRegistrationRequestDto dto) {

        if (requestRepository.existsByEmail(dto.getEmail()))
            throw new IllegalStateException("A request with this email already exists");

        if (userRepository.existsByEmail(dto.getEmail()))
            throw new IllegalStateException("This email is already registered");

        SchoolRequest request = SchoolRequest.builder()
            .schoolName(dto.getSchoolName())
            .ownerFullName(dto.getOwnerFullName())
            .phone(dto.getPhone())
            .email(dto.getEmail())
            .wilaya(dto.getWilaya())
            .commune(dto.getCommune())
            .address(dto.getAddress())
            .password(passwordEncoder.encode(dto.getPassword()))  // hash immediately
         
            .status(RequestStatus.PENDING)
            .build();

        requestRepository.save(request);
        return mapRequestToResponse(request);
    }

    // ======== SUPER ADMIN — view all pending requests ========

    public Page<SchoolRequestResponseDto> getPendingRequests(Pageable pageable) {
        return requestRepository
            .findAllByStatus(RequestStatus.PENDING, pageable)
            .map(this::mapRequestToResponse);
    }

    // ======== SUPER ADMIN — approve request ========
    // Creates School + User(SCHOOL_ADMIN) + SchoolAdminProfile atomically

    public SchoolResponseDto approveRequest(Long requestId, User superAdmin) {

        SchoolRequest request = requestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + requestId));

        if (request.getStatus() != RequestStatus.PENDING)
            throw new IllegalStateException("Request is not pending");

    

        // 1. Create School
        School school = School.builder()
            .schoolName(request.getSchoolName())
            .ownerName(request.getOwnerFullName())
            .phone(request.getPhone())
            .email(request.getEmail())
            .wilaya(request.getWilaya())
            .commune(request.getCommune())
            .address(request.getAddress())
         
            .subscriptionStatus(SubscriptionStatus.TRIAL)              // 30-day trial
            .subscriptionExpiresAt(LocalDate.now().plusDays(30))
            .build();

        schoolRepository.save(school);

        // 2. Create User for school admin
        User adminUser = User.builder()
            .fullName(request.getOwnerFullName())
            .email(request.getEmail())
            .password(request.getPassword())                            // already hashed
            .role(Role.SCHOOL_ADMIN)
            .enabled(true)
            .build();

        userRepository.save(adminUser);
             
        // 3. Create SchoolAdminProfile
        SchoolAdminProfile adminProfile = SchoolAdminProfile.builder()
            .user(adminUser)
            .school(school)
            .phone(request.getPhone())
            .build();

        adminProfileRepository.save(adminProfile);

        // 4. Update request
        request.setStatus(RequestStatus.APPROVED);
        request.setReviewedAt(LocalDateTime.now());
      
        requestRepository.save(request);

        // 5. Audit log
        logAction(superAdmin, AdminActionType.APPROVE_SCHOOL_REQUEST,
            requestId, "SchoolRequest", null);

        return mapSchoolToResponse(school);
    }

    // ======== SUPER ADMIN — reject request ========

    public void rejectRequest(Long requestId, String comment, User superAdmin) {

        SchoolRequest request = requestRepository.findById(requestId)
            .orElseThrow(() -> new ResourceNotFoundException("Request not found: " + requestId));

        if (request.getStatus() != RequestStatus.PENDING)
            throw new IllegalStateException("Request is not pending");

        request.setStatus(RequestStatus.REJECTED);
        request.setReviewedAt(LocalDateTime.now());
        request.setReviewComment(comment);
        requestRepository.save(request);

        logAction(superAdmin, AdminActionType.REJECT_SCHOOL_REQUEST,
            requestId, "SchoolRequest", comment);
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

    private SchoolRequestResponseDto mapRequestToResponse(SchoolRequest r) {
        return SchoolRequestResponseDto.builder()
            .id(r.getId())
            .schoolName(r.getSchoolName())
            .ownerFullName(r.getOwnerFullName())
            .phone(r.getPhone())
            .email(r.getEmail())
            .wilaya(r.getWilaya())
            .commune(r.getCommune())
            .status(r.getStatus())
          
            .createdAt(r.getCreatedAt())
            .reviewedAt(r.getReviewedAt())
            .reviewComment(r.getReviewComment())
            .build();
    }
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
}

