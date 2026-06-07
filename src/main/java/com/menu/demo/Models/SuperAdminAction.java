package com.menu.demo.Models;

import java.time.LocalDateTime;

import com.menu.demo.Enums.AdminActionType;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class SuperAdminAction {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "admin_user_id", nullable = false)
    private User superAdmin;

    @Enumerated(EnumType.STRING)
    private AdminActionType actionType;

    private Long targetEntityId;
    private String targetEntityType;     // "SchoolRequest", "School", ...
    private String comment;

    @Builder.Default
    private LocalDateTime performedAt = LocalDateTime.now();
}
