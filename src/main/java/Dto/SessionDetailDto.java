package Dto;

import java.time.LocalDate;
import java.time.LocalTime;

import com.menu.demo.Enums.PricingModel;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class SessionDetailDto {
    private Long     id;
    private Long     moduleId;
    private String   moduleName;
    private String   subjectName;
    private String   teacherName;
    private String   level;
    private Long     schoolId;
    private LocalDate  date;
    private String   dayOfWeek;          // "FRIDAY"
    private LocalTime  startTime;
    private LocalTime  endTime;
    private boolean  archived;
    private int      enrolledCount;
    private boolean  attendanceMarked;  
    private PricingModel pricingModel;
}