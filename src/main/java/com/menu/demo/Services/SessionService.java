package com.menu.demo.Services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.menu.demo.Enums.Role;
import com.menu.demo.Exceptions.ResourceNotFoundException;
import com.menu.demo.Models.School;
import com.menu.demo.Models.SchoolAdminProfile;
import com.menu.demo.Models.Session;
import com.menu.demo.Models.Subject;
import com.menu.demo.Models.TeacherProfile;
import com.menu.demo.Models.User;

import com.menu.demo.Repositories.ClassroomRepository;
import com.menu.demo.Repositories.CourseModuleRepository;
import com.menu.demo.Repositories.SchoolRepository;
import com.menu.demo.Repositories.SessionRepository;
import com.menu.demo.Repositories.Subjectrepository;
import com.menu.demo.Repositories.TeacherRepository;
import com.menu.demo.Models.ClassRoom;
import com.menu.demo.Models.CourseModule;

import Dto.SessionRequestDto;
import Dto.SessionResponseDto;
import Dto.SessionUpdateDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionService {
	
	private final SessionRepository sessionRepository;
	private final TeacherRepository teacherRepository;
	private final Subjectrepository subjectRepository;
	private final ClassroomRepository classRepository;
	private final SchoolRepository schoolRepository;
	private final CourseModuleRepository courseModuleRepository;
	

//  ============================= CREATE SESSION ====================
	@Transactional
	public SessionResponseDto createSession(SessionRequestDto request, SchoolAdminProfile admin) {

	    School school = admin.getSchool();
        CourseModule course=courseModuleRepository.findByName(request.getCourseModuleName()).orElseThrow(()->new ResourceNotFoundException("CourseModule Not Found With Name : "+request.getCourseModuleName()));
	  

	    Session session = Session.builder()
	        .school(school)
	        .module(course)
	        
	        .date(request.getDate())
	        .startTime(request.getStartTime())
	        .endTime(request.getEndTime())
	        .archived(false)
	        .build();

	    sessionRepository.save(session);

	    return mapToResponse(session);
	}
  
//  ====================== GET SISSIONS BY SCHOOL ====================
  
  public List<SessionResponseDto> getAllSessions(Long schoolId){
	  School school=schoolRepository.findById(schoolId).orElseThrow(()->new ResourceNotFoundException("school Not found with id : "+schoolId));
  	
  	return sessionRepository.findAllBySchoolAndArchivedFalse(school).stream().map(this::mapToResponse).collect(Collectors.toList());
  }
  
//  =================  UPDATE SESSION ===========================
  
  public ResponseEntity<SessionResponseDto>    updateSession(Long id,SchoolAdminProfile admin,SessionUpdateDto dto){
  	
    
  	Session session =sessionRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Session Not Found By id " +id));
  	if(!session.getSchool().getId().equals(admin.getSchool().getId()))
  	    throw new AccessDeniedException("Not your school");
  	TeacherProfile teacher = teacherRepository.findById(dto.getTeacherId()).orElseThrow(()->new ResourceNotFoundException("Session Not Found By id " +dto.getTeacherId()));
   
  	session.setDate(dto.getDate());
  	session.setEndTime(dto.getEndTime());
  	session.setStartTime(dto.getStartTime());
  	sessionRepository.save(session);
  	
  	
       return ResponseEntity.ok(mapToResponse(session));     
  	
  	
  }
 
  
  
//====================== DELETE LOGICALLY THE Session =============================
  
  public ResponseEntity<SessionResponseDto> archiveSession(Long id ,SchoolAdminProfile schoolAdmin){
  	
 
  
  	Session session=sessionRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Session Not Found By id " +id));
  	if(!session.getSchool().getId().equals(schoolAdmin.getSchool().getId()))
  	    throw new AccessDeniedException("Not your school");
  	session.setArchived(true);
  	sessionRepository.save(session);
  	
  	return ResponseEntity.ok(mapToResponse(session));
  	
  }
  
  
  
  

  private SessionResponseDto mapToResponse(Session session) {
      return SessionResponseDto.builder()
    		  .id(session.getId())
    	      .moduleId(session.getModule().getId())
    		  .date(session.getDate())
    		  .endTime(session.getEndTime())
    		  .isArchived(session.isArchived())
    		  .schoolId(session.getSchool().getId())
    		  .startTime(session.getStartTime()).build();
    		 
  }

}
