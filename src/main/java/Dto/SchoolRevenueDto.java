package Dto;

import java.math.BigDecimal;
import java.time.YearMonth;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class SchoolRevenueDto {
    private YearMonth period;
    private BigDecimal totalExpected;
    private BigDecimal totalCollected;
    private BigDecimal totalPending;
    private BigDecimal totalOverdue;
    private int invoiceCount;
    private List<StudentInvoiceResponseDto> invoices;
}
