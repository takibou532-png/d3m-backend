package Dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

import com.menu.demo.Enums.InvoiceStatus;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class SchoolInvoiceResponseDto {
    private Long id;
    private String schoolName;
    private YearMonth period;
    private String academicYear;
    private BigDecimal amount;
    private InvoiceStatus status;
    private LocalDate dueDate;
    private LocalDateTime paidAt;
}
