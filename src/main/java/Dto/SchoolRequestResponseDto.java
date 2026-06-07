package Dto;

import java.time.LocalDateTime;

import com.menu.demo.Enums.RequestStatus;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class SchoolRequestResponseDto {
    private Long id;
    private String schoolName;
    private String ownerFullName;
    private String phone;
    private String email;
    private String wilaya;
    private String commune;
    private RequestStatus status;
 
    private LocalDateTime createdAt;
    private LocalDateTime reviewedAt;
    private String reviewComment;
}
