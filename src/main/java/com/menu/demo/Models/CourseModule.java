package com.menu.demo.Models;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import com.menu.demo.Enums.PricingModel;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class CourseModule {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    private School school;

    @ManyToOne(fetch = FetchType.LAZY)
    private Subject subject;

    @ManyToOne(fetch = FetchType.LAZY)
    private TeacherProfile teacher;

    @ManyToOne(fetch = FetchType.LAZY)
    private ClassRoom classroom;

    private String level;
    @Column(unique=true)
    private String name;

   
    @OneToMany(mappedBy = "module", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ModuleSchedule> schedules;    // one entry per day
    private BigDecimal monthlyprice; 
    private LocalDate periodStart;
    private LocalDate periodEnd;
    private Integer maxStudents;
    private boolean archived;
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private PricingModel pricingModel = PricingModel.MONTHLY_FLAT;

    
    private BigDecimal pricePerSession;

    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}
