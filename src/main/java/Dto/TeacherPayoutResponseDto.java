package Dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

import com.menu.demo.Enums.PayoutStatus;

import lombok.*;

@Data @Builder
public class TeacherPayoutResponseDto {
    private Long id;
    private Long teacherId;
    private String teacherName;
    private YearMonth period;
    private BigDecimal totalModuleRevenue;   // what students paid for their modules
    private BigDecimal percentage;           // teacher's cut %
    private BigDecimal payoutAmount;         // what the teacher gets
    private PayoutStatus status;
    private LocalDateTime paidAt;
    private LocalDateTime calculatedAt;
}
