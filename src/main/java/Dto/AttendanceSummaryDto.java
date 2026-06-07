package Dto;

import java.util.List;

import lombok.Builder;
import lombok.Data;
@Data @Builder

public class AttendanceSummaryDto {
	  private int totalSessions;
	    private long present;
	    private long absent;
	   
	    private long attendanceRate;       // percentage 0-100
	    private List<AttendanceResponseDto> records;
}
