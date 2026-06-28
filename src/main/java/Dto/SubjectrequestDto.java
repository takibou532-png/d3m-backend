package Dto;

import lombok.Data;

@Data
public class SubjectrequestDto {
    private String name;
    private String description;
    private Long teacherId;   // optional — assign teacher at subject creation
}