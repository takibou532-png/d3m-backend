package Dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class ModuleScheduleViewDto {
    private Long   moduleId;
    private String moduleName;
    private String subjectName;
    private String teacherName;
    private String level;           
    private String day;             
    private String startTime;      
    private String endTime;        
    private int    maxStudents;
    private long   enrolledCount;
    private boolean full;          
    private BigDecimal monthlyPrice;
}