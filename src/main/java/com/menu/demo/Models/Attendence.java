package com.menu.demo.Models;

import java.time.LocalDateTime;

import com.menu.demo.Enums.AttendenceStatus;

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
	        columnNames = {"student_id", "session_id"}  ))
	    
public class Attendence {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch=FetchType.LAZY)
    private StudentProfile student;

    @ManyToOne(fetch=FetchType.LAZY)
    private Session session;

    @Enumerated(EnumType.STRING)
    private AttendenceStatus status;
    @Builder.Default
    private LocalDateTime markedAt = LocalDateTime.now();
    private String note;

  
}
