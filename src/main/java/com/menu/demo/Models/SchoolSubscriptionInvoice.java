package com.menu.demo.Models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;

import com.menu.demo.Enums.InvoiceStatus;

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
@Table(
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"school_id", "period"}      // one platform invoice per school per month
    )
)
public class SchoolSubscriptionInvoice {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "school_id", nullable = false)
    private School school;

    private Integer year;               // e.g. 2025 (academic year start)
    private String academicYear;

   

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Builder.Default
    private InvoiceStatus status = InvoiceStatus.PENDING;     // PENDING, PAID, OVERDUE, CANCELLED

    private LocalDate dueDate;
    private LocalDateTime paidAt;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
