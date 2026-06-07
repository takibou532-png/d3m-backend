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



import com.menu.demo.Repositories.Subjectrepository;


import Dto.SubjectResponseDto;
import Dto.SubjectrequestDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SubjectService {

    private final Subjectrepository subjectRepository;

 

//    ============================= CREATE SUBJECT ====================
    
    public ResponseEntity<SubjectResponseDto> createSubject(SubjectrequestDto request,SchoolAdminProfile admin){
    	
   
    

    	School school=admin.getSchool();
    	
    	Subject subject =Subject.builder().description(request.getDescription())
    			.name(request.getName())
    			.school(school)
    			.isArchived(false)
    			.build();
    	subjectRepository.save(subject);
    	
    	return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(subject));
    	
    }
//    ====================== GET SUBJECTS BY SCHOOL ====================
    
    public List<SubjectResponseDto> getAllSubjects(SchoolAdminProfile admin){
    
    	
    	School school=admin.getSchool();
    	return subjectRepository.findAllBySchoolAndIsArchivedFalse(school).stream().map(this::mapToResponse).collect(Collectors.toList());
    }
    
//    =================  UPDATE SUBJECT ===========================
    
    public SubjectResponseDto    updateSubject(Long id,SchoolAdminProfile admin,SubjectrequestDto dto){
    	
 
    	Subject subject=subjectRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Subject Not Found By id " +id));
    	if(!subject.getSchool().getId().equals(admin.getSchool().getId()))
    	    throw new AccessDeniedException("Not your school");
    	
    	 subject.setDescription(dto.getDescription());
    	 subject.setName(dto.getName());
              subjectRepository.save(subject);
         return mapToResponse(subject);     
    	
    	
    }
   
    
    
// ====================== DELETE LOGICALLY THE SUBJECT =============================
    
    public ResponseEntity<SubjectResponseDto> archiveSubject(Long id ,SchoolAdminProfile schoolAdmin){
    	
    	
    
    	Subject subject=subjectRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Subject Not Found By id " +id));
    	if(!subject.getSchool().getId().equals(schoolAdmin.getSchool().getId()))
    	    throw new AccessDeniedException("Not your school");
    	subject.setArchived(true);
    	subjectRepository.save(subject);
    	
    	return ResponseEntity.ok(mapToResponse(subject));
    	
    }
    
    
    
    

    private SubjectResponseDto mapToResponse(Subject subject) {
        return SubjectResponseDto.builder()
                .description(subject.getDescription())
                .name(subject.getName())
                .id(subject.getId())
                .schoolId(subject.getSchool().getId())
                .isArchived(subject.isArchived())
                .build();
    }
}

