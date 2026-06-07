package com.menu.demo.Services;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.menu.demo.Enums.Role;
import com.menu.demo.Exceptions.ResourceNotFoundException;
import com.menu.demo.Models.ClassRoom;
import com.menu.demo.Models.School;
import com.menu.demo.Models.SchoolAdminProfile;
import com.menu.demo.Models.User;
import com.menu.demo.Repositories.ClassroomRepository;

import Dto.ClassRoomRequestDto;
import Dto.ClassRoomResponsDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class ClassroomService {
	private final ClassroomRepository classroomRepository;
	
	
	

//  ============================= CREATE ClassRoom ====================
  
  public ResponseEntity<ClassRoomResponsDto> createClassRoom(ClassRoomRequestDto request,SchoolAdminProfile admin){
  	
  
  	School school=admin.getSchool();
  	if(classroomRepository.existsByNameAndSchool(request.getName(), school))
  	    throw new IllegalArgumentException("Classroom already exists");
ClassRoom classroom=ClassRoom.builder()
.capacity(request.getCapacity())
.name(request.getName())

.school(school).build();
  	
  			
  			
  	classroomRepository.save(classroom);
  	
  	return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(classroom));
  	}
  	
  
//  ====================== GET SISSIONS BY SCHOOL ====================
  
  public List<ClassRoomResponsDto> getAllClassRooms(SchoolAdminProfile admin){
  
  	
  	School school=admin.getSchool();
  	return classroomRepository.findAllBySchool(school).stream().map(this::mapToResponse).collect(Collectors.toList());
  }
  
//  =================  UPDATE ClassRoom ===========================
  
  public ResponseEntity<ClassRoomResponsDto>    updateClassroom(Long id,SchoolAdminProfile admin,ClassRoomRequestDto dto){
  	
  
  	ClassRoom classroom =classroomRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("ClassRoom Not Found By id " +id));
  	if(!classroom.getSchool().getId().equals(admin.getSchool().getId()))
  	    throw new AccessDeniedException("Not your school");
  	classroom.setCapacity(dto.getCapacity());
  	
  	classroom.setName(dto.getName());
  	classroomRepository.save(classroom);
  	
       return ResponseEntity.ok(mapToResponse(classroom));     
  	
  	
  }
 
  
  
//====================== DELETE LOGICALLY THE SUBJECT =============================
  
//  public ResponseEntity<SessionResponseDto> archiveSession(Long id ,User schoolAdmin){
//  	
//  	if(!Role.SCHOOL_ADMIN.equals(schoolAdmin.getRole()))
//  	    throw new AccessDeniedException("Only school admin");
//  
//  	Session session=sessionRepository.findById(id).orElseThrow(()->new ResourceNotFoundException("Session Not Found By id " +id));
//  	if(!session.getSchool().getId().equals(schoolAdmin.getSchool().getId()))
//  	    throw new AccessDeniedException("Not your school");
//  	session.setArchived(true);
//  	sessionRepository.save(session);
//  	
//  	return ResponseEntity.ok(mapToResponse(session));
//  	
//  }
  
  
  
  

  private ClassRoomResponsDto mapToResponse(ClassRoom classroom) {
      return ClassRoomResponsDto.builder()
    		  .id(classroom.getId())
    		  .capacity(classroom.getCapacity())
    		  .schoolId(classroom.getSchool().getId())
    		  .name(classroom.getName())
    		
    		  .build();
  }

}
