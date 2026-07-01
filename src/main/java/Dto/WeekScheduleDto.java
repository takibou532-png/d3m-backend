package Dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class WeekScheduleDto {
    private LocalDate weekStart;         // Monday of the week
    private LocalDate weekEnd;           // Sunday of the week
    private Map<String, List<SessionDetailDto>> byDay;
    // keys: "MONDAY", "TUESDAY", ... in day order
}
