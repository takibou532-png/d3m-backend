package com.menu.demo.Models;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.YearMonth;

import com.menu.demo.Enums.PayoutStatus;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
@Table(uniqueConstraints =@UniqueConstraint(columnNames = {"teacher_id", "period"}))
public class TeacherPayout {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id", nullable = false)
    private TeacherProfile teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    private YearMonth period;               

    private BigDecimal totalModuleRevenue;  // SUM of paid student invoices for teacher's modules

    private BigDecimal percentage;          // snapshot of teacher's % at calculation time

    private BigDecimal payoutAmount;        // totalModuleRevenue × percentage / 100

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PayoutStatus status = PayoutStatus.PENDING;

    private LocalDateTime paidAt;

    @Builder.Default
    private LocalDateTime calculatedAt = LocalDateTime.now();
}
