package Dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.menu.demo.Enums.EnrollmentStatus;

import com.menu.demo.Models.Session;
import com.menu.demo.Models.StudentProfile;


import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

import lombok.Builder;
import lombok.Data;
@Data @Builder
public class EnrollmentResponseDto {

	    private Long id;

	   
	    private StudentProfile student;
       
	    private String subjectName;
	    private String teacherName;
	    private String moduleName  ;  
	    private Long ModuleId;
	    @Enumerated(EnumType.STRING)
	    private EnrollmentStatus status;

	    private LocalDate startDate;
	    private LocalDate endDate;

	    private BigDecimal monthlyPrice;
	    private LocalDateTime createdAt;

}
