package Dto;

import java.time.LocalDateTime;

import com.menu.demo.Enums.AttendenceStatus;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class StudentAttendanceDto {
    private Long    studentId;
    private String  fullName;
    private String  level;
    private String  parentPhone;
    private AttendenceStatus status;   // null = not marked yet
    private String  note;
    private LocalDateTime markedAt;
}