package Dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class TeacherResponseDto {
	private Long id;
	 private String fullName;
	 
	    private Long schoolId;
	    private Long subjectId;
	    private String email;
	    private String specialization;
	    private boolean archived;
	    private String bio;
}
