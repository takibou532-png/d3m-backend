package Dto;

import java.time.LocalDateTime;

import com.menu.demo.Enums.AttendenceStatus;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class AttendanceResponseDto {
    private Long studentId;
    private String studentName;
    private AttendenceStatus status;   // null = not marked yet
    private String note;
    private LocalDateTime markedAt;
}
