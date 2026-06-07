package Dto;

import java.time.LocalDate;
import java.util.List;

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

   
    private List<ScheduleEntryDto> schedules;
}
