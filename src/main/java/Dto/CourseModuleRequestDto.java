package Dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import com.menu.demo.Enums.PricingModel;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class CourseModuleRequestDto {
    private Long subjectId;
    private Long teacherId;
    private Long classroomId;
    private String level;
    private String name;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Integer maxStudents;
    private PricingModel pricingModel;   
    private BigDecimal   pricePerSession;
   
    private List<ScheduleEntryDto> schedules;
}
