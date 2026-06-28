package Dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SubjectResponseDto {
    private Long id;
    private String name;
    private String description;
    private Long schoolId;
    private boolean isArchived;
    private Long teacherId;       // null if no teacher assigned
    private String teacherName;   // null if no teacher assigned
}