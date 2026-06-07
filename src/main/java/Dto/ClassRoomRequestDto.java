package Dto;



import lombok.Data;

@Data
public class ClassRoomRequestDto {
	private Long schoolId;
    private String name;
    private String level;
    private Integer capacity;

}
