package com.menu.demo.Services;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.menu.demo.Enums.AttendenceStatus;
import com.menu.demo.Enums.Role;
import com.menu.demo.Exceptions.ResourceNotFoundException;
import com.menu.demo.Models.School;
import com.menu.demo.Models.SchoolAdminProfile;
import com.menu.demo.Models.Session;
import com.menu.demo.Models.StudentProfile;
import com.menu.demo.Models.Subject;
import com.menu.demo.Models.TeacherProfile;
import com.menu.demo.Models.User;
import com.menu.demo.Repositories.AttendanceRepository;
import com.menu.demo.Repositories.ClassroomRepository;
import com.menu.demo.Repositories.CourseModuleRepository;
import com.menu.demo.Repositories.EnrollmentRepository;
import com.menu.demo.Repositories.SchoolRepository;
import com.menu.demo.Repositories.SessionRepository;
import com.menu.demo.Repositories.Subjectrepository;
import com.menu.demo.Repositories.TeacherRepository;
import com.menu.demo.Models.Attendence;
import com.menu.demo.Models.ClassRoom;
import com.menu.demo.Models.CourseModule;

import Dto.AttendanceSheetDto;
import Dto.SessionDetailDto;
import Dto.SessionRequestDto;
import Dto.SessionResponseDto;
import Dto.SessionUpdateDto;
import Dto.StudentAttendanceDto;
import Dto.WeekScheduleDto;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@Transactional
public class SessionService {
	
	private final SessionRepository     sessionRepository;
    private final TeacherRepository     teacherRepository;
    private final ClassroomRepository   classRepository;
    private final SchoolRepository      schoolRepository;
    private final CourseModuleRepository courseModuleRepository;
    private final EnrollmentRepository  enrollmentRepository;
    private final AttendanceRepository  attendanceRepository;
	

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
  // ── NEW: get sessions for a specific date ─────────────────────
  // Used by: school admin clicking a calendar day
  //          teacher viewing past sessions to check attendance
  public List<SessionDetailDto> getSessionsByDate(Long schoolId, LocalDate date) {
      School school = schoolRepository.findById(schoolId)
          .orElseThrow(() -> new ResourceNotFoundException("School not found: " + schoolId));

      return sessionRepository.findAllBySchoolAndDate(school, date)
          .stream()
          .map(this::toDetailDto)
          .toList();
  }

  // ── NEW: get full week schedule ───────────────────────────────
  // weekStart: any date — we snap to Monday automatically
  // Returns sessions grouped by day name for the whole week
  public WeekScheduleDto getSessionsByWeek(Long schoolId, LocalDate anyDateInWeek) {

      School school = schoolRepository.findById(schoolId)
          .orElseThrow(() -> new ResourceNotFoundException("School not found: " + schoolId));

      // Snap to Monday of the given week
      LocalDate monday = anyDateInWeek.with(java.time.DayOfWeek.MONDAY);
      LocalDate sunday = monday.plusDays(6);

      List<Session> sessions = sessionRepository
          .findAllBySchoolAndDateRange(school, monday, sunday);

      // Group by day name in correct order
      Map<String, List<SessionDetailDto>> byDay = new java.util.LinkedHashMap<>();
      for (java.time.DayOfWeek dow : java.time.DayOfWeek.values()) {
          String dayName = dow.name();
          List<SessionDetailDto> forDay = sessions.stream()
              .filter(s -> s.getDate().getDayOfWeek().name().equals(dayName))
              .map(this::toDetailDto)
              .toList();
          if (!forDay.isEmpty()) {
              byDay.put(dayName, forDay);
          }
      }

      return WeekScheduleDto.builder()
          .weekStart(monday)
          .weekEnd(sunday)
          .byDay(byDay)
          .build();
  }

  // ── NEW: teacher's own week schedule ──────────────────────────
  public WeekScheduleDto getMyWeekSchedule(User currentUser, LocalDate anyDateInWeek) {

      TeacherProfile teacher = teacherRepository.findByUser(currentUser)
          .orElseThrow(() -> new ResourceNotFoundException("Teacher profile not found"));

      LocalDate monday = anyDateInWeek.with(java.time.DayOfWeek.MONDAY);
      LocalDate sunday = monday.plusDays(6);

      List<Session> sessions = sessionRepository
          .findByTeacherAndDateRange(teacher.getId(), monday, sunday);

      Map<String, List<SessionDetailDto>> byDay = new java.util.LinkedHashMap<>();
      for (java.time.DayOfWeek dow : java.time.DayOfWeek.values()) {
          String dayName = dow.name();
          List<SessionDetailDto> forDay = sessions.stream()
              .filter(s -> s.getDate().getDayOfWeek().name().equals(dayName))
              .map(this::toDetailDto)
              .toList();
          if (!forDay.isEmpty()) byDay.put(dayName, forDay);
      }

      return WeekScheduleDto.builder()
          .weekStart(monday)
          .weekEnd(sunday)
          .byDay(byDay)
          .build();
  }

  // ── NEW: attendance sheet for a session ───────────────────────
  // Returns all enrolled students + their attendance status for this session
  // null status = not marked yet
  public AttendanceSheetDto getAttendanceSheet(Long sessionId, SchoolAdminProfile admin) {

      Session session = sessionRepository.findById(sessionId)
          .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

      if (!session.getSchool().getId().equals(admin.getSchool().getId()))
          throw new AccessDeniedException("Session does not belong to your school");

      return buildAttendanceSheet(session);
  }

  // Teacher version — checks they teach this module
  public AttendanceSheetDto getAttendanceSheetForTeacher(Long sessionId, User currentUser) {

      Session session = sessionRepository.findById(sessionId)
          .orElseThrow(() -> new ResourceNotFoundException("Session not found: " + sessionId));

      TeacherProfile teacher = teacherRepository.findByUser(currentUser)
          .orElseThrow(() -> new ResourceNotFoundException("Teacher profile not found"));

      if (!session.getModule().getTeacher().getId().equals(teacher.getId()))
          throw new AccessDeniedException("You are not the teacher of this session");

      return buildAttendanceSheet(session);
  }

  // ── Shared builder for attendance sheet ──────────────────────
  private AttendanceSheetDto buildAttendanceSheet(Session session) {

      // All enrolled students in this module
      List<StudentProfile> enrolled = enrollmentRepository
          .findActiveStudentsByModuleId(session.getModule().getId());

      // Already marked attendance
      List<Attendence> marked = attendanceRepository.findAllBySession(session);

      Map<Long, Attendence> markedMap = marked.stream()
          .collect(java.util.stream.Collectors.toMap(
              a -> a.getStudent().getId(), a -> a
          ));

      List<StudentAttendanceDto> students = enrolled.stream()
          .map(sp -> {
              Attendence a = markedMap.get(sp.getId());
              return StudentAttendanceDto.builder()
                  .studentId(sp.getId())
                  .fullName(sp.getUser().getFullName())
                  .level(sp.getLevel())
                  .parentPhone(sp.getParentPhone())
                  .status(a != null ? a.getStatus() : null)
                  .note(a != null ? a.getNote() : null)
                  .markedAt(a != null ? a.getMarkedAt() : null)
                  .build();
          })
          .toList();

      long present   = students.stream().filter(s -> s.getStatus() == AttendenceStatus.PRESENT).count();
      long absent    = students.stream().filter(s -> s.getStatus() != null && s.getStatus() != AttendenceStatus.PRESENT).count();
      long notMarked = students.stream().filter(s -> s.getStatus() == null).count();

      return AttendanceSheetDto.builder()
          .sessionId(session.getId())
          .moduleName(session.getModule().getName())
          .subjectName(session.getModule().getSubject().getName())
          .teacherName(session.getModule().getTeacher().getUser().getFullName())
          .date(session.getDate())
          .startTime(session.getStartTime())
          .endTime(session.getEndTime())
          .totalEnrolled(enrolled.size())
          .presentCount(present)
          .absentCount(absent)
          .notMarkedCount(notMarked)
          .students(students)
          .build();
  }

  // ── Mapping ───────────────────────────────────────────────────
  private SessionDetailDto toDetailDto(Session s) {
      long enrolled = enrollmentRepository.countActiveByModule(s.getModule());
      boolean hasAttendance = attendanceRepository.existsBySession(s);

      return SessionDetailDto.builder()
          .id(s.getId())
          .moduleId(s.getModule().getId())
          .moduleName(s.getModule().getName())
          .subjectName(s.getModule().getSubject().getName())
          .teacherName(s.getModule().getTeacher().getUser().getFullName())
          .level(s.getModule().getLevel())
          .schoolId(s.getSchool().getId())
          .date(s.getDate())
          .dayOfWeek(s.getDate().getDayOfWeek().name())
          .startTime(s.getStartTime())
          .endTime(s.getEndTime())
          .archived(s.isArchived())
          .enrolledCount((int) enrolled)
          .attendanceMarked(hasAttendance)
          .pricingModel(s.getModule().getPricingModel())
          .build();
  }
  
  
  
  

  private SessionResponseDto mapToResponse(Session session) {
      return SessionResponseDto.builder()
    		  .id(session.getId())
    	      .moduleId(session.getModule().getId())
    		  .date(session.getDate())
    		  .endTime(session.getEndTime())
    		  .Archived(session.isArchived())
    		  .schoolId(session.getSchool().getId())
    		  .startTime(session.getStartTime()).build();
    		 
  }

}
