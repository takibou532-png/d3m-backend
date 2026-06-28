package Dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class SchoolBrowseCardDto {
    private Long   schoolId;
    private String schoolName;
    private String wilaya;
    private String email;
    private String phone;
    private String commune;
    private long   totalModules;
    private long   totalTeachers;
}
