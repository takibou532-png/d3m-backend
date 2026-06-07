package Dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class PlatformDashboardDto {
    private long totalSchools;
    private long activeSchools;
    private long suspendedSchools;
    private long trialSchools;
    private long pendingRequests;
    private long expiredSchools;
    private BigDecimal currentYearRevenue;
    private BigDecimal allTimeRevenue;
    private List<SchoolInvoiceResponseDto> recentInvoices;
    private List<SchoolRequestResponseDto> pendingSchoolRequests;
}
