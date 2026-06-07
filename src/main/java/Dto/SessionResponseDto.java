package Dto;

import java.time.LocalDate;
import java.time.LocalTime;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SessionResponseDto {
  
    private Long id;
    private Long moduleId;
    private Long schoolId;
    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean isArchived;
}
