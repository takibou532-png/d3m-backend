package Dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ClassRoomResponsDto {
	private Long id;
	private Long schoolId;
    private String name;
 
    private Integer capacity;

}
