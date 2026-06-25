package Dto;

import lombok.Builder;
import lombok.Data;

@Data @Builder
public class OtpVerifiedResponse {
    private String resetToken;   // JWT, valid 15 min, only for reset
    private String message;
}
