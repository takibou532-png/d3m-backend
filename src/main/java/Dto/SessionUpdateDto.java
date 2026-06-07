package Dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Data;
@Data
public class SessionUpdateDto {
	    private Long teacherId;
	    private LocalDate date;
	    private LocalTime startTime;
	    private LocalTime endTime;

}
