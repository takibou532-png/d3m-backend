package com.menu.demo.Services;


import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.menu.demo.Enums.EnrollmentStatus;
import com.menu.demo.Enums.Role;
import com.menu.demo.Exceptions.ResourceNotFoundException;
import com.menu.demo.Models.CourseModule;
import com.menu.demo.Models.Enrollment;
import com.menu.demo.Models.SchoolAdminProfile;
import com.menu.demo.Models.StudentProfile;
import com.menu.demo.Models.StudentRequest;
import com.menu.demo.Models.User;
import com.menu.demo.Repositories.CourseModuleRepository;
import com.menu.demo.Repositories.EnrollmentRepository;

import com.menu.demo.Repositories.StudentRepository;
import com.menu.demo.Repositories.StudentRequestRepository;
import com.menu.demo.Repositories.UserRepository;

import Dto.EnrollmentResponseDto;
import Dto.StudentEnrollmentRequestDto;
import Dto.StudentRegistrationRequest;
import Dto.StudentRequestDto;
import Dto.StudentResponseDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor

public class StudentService {

    private final StudentRepository studentProfileRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EnrollmentRepository enrollmentRepository;

    private final CourseModuleRepository moduleRepository;
  

  
    public ResponseEntity<StudentResponseDto> getCurrentStudentProfile(User currentUser) {
    	
    	if(currentUser.getRole() != Role.STUDENT)
    	    throw new AccessDeniedException("Only students allowed");
    	
        StudentProfile profile = studentProfileRepository.findByUser(currentUser)
                .orElseThrow(() -> 
                    new RuntimeException("Student profile not found")
                );

        return ResponseEntity.ok(mapToResponse(profile));
    }

   
    public StudentResponseDto updateCurrentStudentProfile(
            User currentUser,
            StudentRequestDto request
    ) {
    	if(currentUser.getRole() != Role.STUDENT)
    	    throw new AccessDeniedException("Only students allowed");

        StudentProfile profile = studentProfileRepository.findByUser(currentUser)
                .orElseThrow(() -> 
                    new RuntimeException("Student profile not found")
                );

        profile.setParentName(request.getParentName());
        profile.setParentPhone(request.getParentPhone());
        currentUser.setFullName(request.getFullName());
        profile.setUser(currentUser);
        studentProfileRepository.save(profile);

        return mapToResponse(profile);
    }
    
    
    @Transactional
    public StudentResponseDto studentRegistration(StudentRegistrationRequest dto) {
    	if (userRepository.existsByEmail(dto.getEmail())) {
    	    throw new RuntimeException("Email already exists");
    	}
    	User user=User.builder().fullName(dto.getFullName()).email(dto.getEmail())
    			.password(passwordEncoder.encode(dto.getPassword())).role(Role.STUDENT).build();
//    	create the student profile corresponding this 
    	userRepository.save(user);
    	StudentProfile student=StudentProfile.builder().archived(false).birthDate(dto.getBirthDate())
    			.level(dto.getLevel()).user(user).parentName(dto.getParentName()).parentPhone(dto.getParentPhone())
    			.build();
    	studentProfileRepository.save(student);
    	
    	return mapToResponse(student);
    			
    	
    	
    }

 // GET STUDENT BY SESSIONS 
//    public List<StudentResponseDto> getStudentsBySession(Long sessionId) {
//        return enrollmentRepository.findActiveStudentsBySessionId(sessionId).stream().map(this::mapToResponse).collect(Collectors.toList());
//        
//    }
    


    // ===== GET STUDENTS IN A MODULE (for teacher/admin attendance sheet) =====

    public List<StudentResponseDto> getStudentsByModule(Long moduleId, SchoolAdminProfile admin) {

        CourseModule module = moduleRepository.findById(moduleId)
            .orElseThrow(() -> new ResourceNotFoundException("Module not found"));

        if (!module.getSchool().getId().equals(admin.getSchool().getId()))
            throw new AccessDeniedException("Not your school");

        return enrollmentRepository.findActiveStudentsByModuleId(moduleId)
            .stream()
            .map(this::mapToResponse)
            .toList();
    }



  
  
  
    
    
    
    
    
    
  

    /* ======================
       Mapping
       ====================== */

    private StudentResponseDto mapToResponse(StudentProfile profile) {

        return StudentResponseDto.builder()
                .id(profile.getId())
                .fullName(profile.getUser().getFullName())
                .email(profile.getUser().getEmail())
                .level(profile.getLevel())
                .parentName(profile.getParentName())
                .parentPhone(profile.getParentPhone())
                .archived(profile.isArchived())
                .build();
    }
}

