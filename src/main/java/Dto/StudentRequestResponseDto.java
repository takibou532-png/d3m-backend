package Dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.menu.demo.Enums.EnrollmentStatus;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class StudentRequestResponseDto {
    private Long id;

    // Student info
    private Long studentId;
    private String studentFullName;
    private String studentEmail;
    private String studentLevel;
    private String parentName;
    private String parentPhone;

    // Module info
    private Long moduleId;
    private String moduleName;
    private String subjectName;
    private String level;
    private BigDecimal monthlyPrice;

    // Request info
    private EnrollmentStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    private String reviewComment;
}
