package Dto;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.menu.demo.Enums.SubscriptionStatus;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class SchoolResponseDto {
    private Long id;
    private String schoolName;
    private String ownerName;
    private String email;
    private String wilaya;
    private SubscriptionStatus subscriptionStatus;
   
    private LocalDate subscriptionExpiresAt;
    private LocalDateTime createdAt;
    
    private long totalStudents;
    private long totalTeachers;
    private BigDecimal currentMonthRevenue;
}
