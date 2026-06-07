package Dto;

import com.menu.demo.Enums.SubscriptionStatus;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

public class SubscruptionRequestDto {
    @Enumerated(EnumType.STRING)
    private SubscriptionStatus subscriptionStatus;
}
