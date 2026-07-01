package Dto;


import java.math.BigDecimal;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import lombok.Data;
@Data
public class TeacherRequestDto {
    private String fullName;
    private String password;
  
    private String email;
    private String specialization;
  
    private String bio;
    @DecimalMin("0.0")
    @DecimalMax("100.0")
    private BigDecimal percentage;
      private Long subjectId;


}
