package Dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class StudentResponseDto {
    private Long id;
    private String fullName;
    private String email;
    private String level;
    private String parentName;
    private String parentPhone;
    private boolean archived;
}
