package Dto;

import lombok.Data;

@Data
public class SubjectrequestDto {
    private Long schoolId;
    private String name;
    private String description;
    private boolean isArchived;

}
