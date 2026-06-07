package com.menu.demo.Models;

import java.time.LocalDate;
import java.time.LocalDateTime;

import com.menu.demo.Enums.EnrollmentStatus;

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
public class StudentRequest {

	  @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
	    private Long id;

	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "student_id")
	    private StudentProfile student;      

	    @ManyToOne(fetch = FetchType.LAZY)
	    @JoinColumn(name = "module_id", nullable = false)
	    private CourseModule module;          

	

	    @Enumerated(EnumType.STRING)
	    @Builder.Default
	    private EnrollmentStatus status = EnrollmentStatus.PENDING;

	    @Builder.Default
	    private LocalDateTime createdAt = LocalDateTime.now();
	    private LocalDateTime reviewedAt;
	    private String reviewComment;
}