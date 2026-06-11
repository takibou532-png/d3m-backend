package Dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class SchoolPayoutSummaryDto {
    private YearMonth period;
    private BigDecimal totalPayoutsDue;       // sum of all PENDING payouts
    private BigDecimal totalPayoutsPaid;      // sum of all PAID payouts
    private int teacherCount;
    private List<TeacherPayoutResponseDto> payouts;
}