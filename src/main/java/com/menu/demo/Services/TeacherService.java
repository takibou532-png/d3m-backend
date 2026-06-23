package com.menu.demo.Services;


import java.math.BigDecimal;
import java.util.List;

import org.apache.coyote.BadRequestException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.menu.demo.Enums.Role;
import com.menu.demo.Exceptions.EmailAlreadyExistsException;
import com.menu.demo.Exceptions.ResourceNotFoundException;
import com.menu.demo.Models.School;
import com.menu.demo.Models.SchoolAdminProfile;
import com.menu.demo.Models.TeacherProfile;
import com.menu.demo.Models.User;
import com.menu.demo.Repositories.SchoolRepository;
import com.menu.demo.Repositories.TeacherRepository;
import com.menu.demo.Repositories.UserRepository;

import Dto.TeacherRequestDto;
import Dto.TeacherResponseDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
@Service
@RequiredArgsConstructor
public class TeacherService {
	
	private final TeacherRepository teacherRepository;
	private final PasswordEncoder passwordEncoder;
	private final SchoolRepository schoolRepository;
	private final UserRepository userRepository;
	
	  
    public TeacherResponseDto getCurrentTeacherProfile(User currentUser) {
    	  if(currentUser.getRole() != Role.TEACHER)
      	    throw new AccessDeniedException("Only Teachers allowed");

        TeacherProfile profile = teacherRepository.findByUser(currentUser)
                .orElseThrow(() -> 
                    new RuntimeException("teacher profile not found")
                );
      

        return (mapToResponse(profile));
    }

   
    public TeacherResponseDto updateCurrentTeacherProfile(
            User currentUser,
            TeacherRequestDto request
    ) {
    	if(currentUser.getRole() != Role.SCHOOL_ADMIN)
    	    throw new AccessDeniedException("Only school admin allowed");

        TeacherProfile profile = teacherRepository.findByUser(currentUser)
                .orElseThrow(() -> 
                    new ResourceNotFoundException("Teacher profile not found")
                );
        
        
        currentUser.setFullName(request.getFullName());
        userRepository.save(currentUser);
      
       profile.setBio(request.getBio());
     	teacherRepository.save(profile);

        return mapToResponse(profile);
    }

  
   public List<TeacherResponseDto> getTeachersBySchool(SchoolAdminProfile schoolAdmin) {
    School school = schoolAdmin.getSchool();
    return teacherRepository
            .findAllBySchoolAndArchivedFalse(school)  // ← only active teachers
            .stream()
            .map(this::mapToResponse)
            .toList();
}


   
    public List<TeacherResponseDto> getArchivedTeachers(SchoolAdminProfile schoolAdmin) {
    School school = schoolAdmin.getSchool();
    return teacherRepository
            .findAllBySchoolAndArchivedTrue(school)
            .stream()
            .map(this::mapToResponse)
            .toList();
}
//0--------------archived--------------------------------------//
@Transactional
public void unarchiveTeacher(Long teacherId, SchoolAdminProfile admin) {
    TeacherProfile profile = teacherRepository.findById(teacherId)
        .orElseThrow(() -> new ResourceNotFoundException("Teacher not found: " + teacherId));

    if (!profile.getSchool().getId().equals(admin.getSchool().getId()))
        throw new AccessDeniedException("Teacher does not belong to your school");

    User user = profile.getUser();
    user.setEnabled(true);
    userRepository.save(user);

    profile.setArchived(false);
    teacherRepository.save(profile);
}
       
    //========================== Create Teacher Profile ================================
    @Transactional
    public ResponseEntity<TeacherResponseDto> creacteTeacherProfile(TeacherRequestDto request,SchoolAdminProfile currentUser)  {
    
    	
    	School shcool = schoolRepository.findById(currentUser.getSchool().getId())
                 .orElseThrow(() -> 
                     new ResourceNotFoundException("School not found with Id" + currentUser.getSchool().getId())
                 );
  

    	
    	if(userRepository.existsByEmail(request.getEmail())){
    	    throw new EmailAlreadyExistsException("Email already exists");
    	}

    	User user=User.builder().email(request.getEmail())
    			.fullName(request.getFullName())
    			.password(passwordEncoder.encode(request.getPassword())).role(Role.TEACHER).build();
    	 userRepository.save(user);
    	 TeacherProfile profile = TeacherProfile.builder()
    			    .user(user)
    			    .school(currentUser.getSchool())
    			    .specialization(request.getSpecialization())
    			    .bio(request.getBio())
    			    .percentage(request.getPercentage() != null
    			        ? request.getPercentage()
    			        : BigDecimal.ZERO)           
    			    .archived(false)
    			    .build();
    	teacherRepository.save(profile);
    	return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(profile));
    			
    	
    			
    	
    	
    }

  

    @Transactional
    public void archiveTeacher(Long teacherId, SchoolAdminProfile admin) {

        TeacherProfile profile = teacherRepository.findById(teacherId)
            .orElseThrow(() -> new ResourceNotFoundException("Teacher not found: " + teacherId));

        // Ownership check — can't archive teachers from other schools
        if (!profile.getSchool().getId().equals(admin.getSchool().getId()))
            throw new AccessDeniedException("Teacher does not belong to your school");

        // Disable login
        User user = profile.getUser();
        user.setEnabled(false);
        userRepository.save(user);

        // Mark profile archived
        profile.setArchived(true);
        teacherRepository.save(profile);
    }
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	// MAPPING TO RESPONSEDTO
    private TeacherResponseDto mapToResponse(TeacherProfile profile) {
    return TeacherResponseDto.builder()
            .id(profile.getId())
            .fullName(profile.getUser().getFullName())
            .email(profile.getUser().getEmail())
            .bio(profile.getBio())
            .specialization(profile.getSpecialization())
            .schoolId(profile.getSchool() != null ? profile.getSchool().getId() : null)
            .archived(profile.isArchived())
            .build();
}

}
