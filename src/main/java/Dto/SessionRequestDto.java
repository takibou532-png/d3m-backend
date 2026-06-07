package Dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;
@Data
public class SessionRequestDto {
	    private Long schoolId;
	    private String courseModuleName;
	    private LocalDate date;
	    private LocalTime startTime;
	    private LocalTime endTime;
	    private boolean isArchived;
	}


