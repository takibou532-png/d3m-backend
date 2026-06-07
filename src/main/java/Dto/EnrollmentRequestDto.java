package Dto;

import java.time.LocalDate;

import com.menu.demo.Enums.EnrollmentStatus;


import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;
@Data
public class EnrollmentRequestDto {
    private Long id;
    private Long studentId;
    private Long SessionId;
    @Enumerated(EnumType.STRING)
    private EnrollmentStatus status;

    private LocalDate startDate;
    private LocalDate endDate;

    private Double monthlyPrice;

}
