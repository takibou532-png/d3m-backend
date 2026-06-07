package com.menu.demo.Models;

import java.time.LocalDateTime;

import com.menu.demo.Enums.EnrollmentStatus;
import com.menu.demo.Enums.RequestStatus;

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
public class SchoolRequest {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String schoolName;
    private String ownerFullName;
    private String phone;
    private String email;
   
    private String wilaya;
    private String commune;
    private String address;
    private String password;

    @Enumerated(EnumType.STRING)
   
    private RequestStatus status;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
    private LocalDateTime reviewedAt;

    private String reviewComment;
}