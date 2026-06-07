package Dto;

import com.menu.demo.Enums.AttendenceStatus;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class AttendanceEntryDto {
	 private Long studentId;
	    private AttendenceStatus status;
	  
}
