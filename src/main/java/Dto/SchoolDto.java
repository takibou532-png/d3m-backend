package Dto;

import com.menu.demo.Enums.SubscriptionStatus;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import lombok.Data;

@Data
public class SchoolDto {
	private String schoolName;
    private String ownerName;
    private String phone;
    private String email;
    private String address;
    private String wilaya;
    private String commune;
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus subscriptionStatus;
}
