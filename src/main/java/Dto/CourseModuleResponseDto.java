package Dto;

import java.time.LocalDate;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class CourseModuleResponseDto {
    private Long id;
    private String name;
    private String level;
    private Long subjectId;
    private String subjectName;
    private Long teacherId;
    private String teacherName;
    private Long classroomId;
    private String classroomName;
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Integer maxStudents;
    private Integer enrolledCount;
    private List<ScheduleEntryDto> schedules;
    private boolean archived;
}
