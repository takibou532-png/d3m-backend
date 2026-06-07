package Dto;

import java.time.DayOfWeek;
import java.time.LocalTime;

import lombok.Data;

@Data
public class ScheduleEntryDto {
	  private DayOfWeek day;          // FRIDAY
	    private LocalTime startTime;    // 08:00
	    private LocalTime endTime;
}
