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
	        columnNames = {"enrollment_id", "period"}  // one invoice per enrollment per month
	    ))
public class Invoice {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private Enrollment enrollment;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private School school;

    @ManyToOne(fetch=FetchType.LAZY)
    private StudentProfile student;

    private YearMonth period;
    private BigDecimal totalAmount;

    @Enumerated(EnumType.STRING)
    private InvoiceStatus status;

    private LocalDate dueDate;
    private LocalDateTime paidAt;
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
