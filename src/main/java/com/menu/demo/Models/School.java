package com.menu.demo.Models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

import com.menu.demo.Enums.SubscriptionStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class School {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String schoolName;
    private String ownerName;
    private String phone;
    private String email;
    private String address;
    private String wilaya;
    private String commune;
    private BigDecimal yearlyPrice; 

    @Enumerated(EnumType.STRING)
    private SubscriptionStatus subscriptionStatus;
    private LocalDate subscriptionExpiresAt; 
     @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
     
}
