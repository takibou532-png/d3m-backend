package Dto;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class AttendanceSheetDto {
    private Long      sessionId;
    private String    moduleName;
    private String    subjectName;
    private String    teacherName;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private int       totalEnrolled;
    private long      presentCount;
    private long      absentCount;
    private long      notMarkedCount;
    private List<StudentAttendanceDto> students;
}