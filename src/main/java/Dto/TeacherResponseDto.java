package Dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeacherResponseDto {
    private Long id;
    private String fullName;
    private Long schoolId;
    private String email;
    private String specialization;
    private boolean archived;
    private String bio;
    private List<Long>   subjectIds;    // all subjects assigned to this teacher
    private List<String> subjectNames;  // for display in frontend
}