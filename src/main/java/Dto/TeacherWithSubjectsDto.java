package Dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class TeacherWithSubjectsDto {
    private Long   teacherId;
    private String fullName;
    private String specialization;
    private String bio;
    // subjects this teacher teaches in this school
    private List<String> subjectNames;
}
