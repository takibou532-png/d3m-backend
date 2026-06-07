package com.menu.demo.Models;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data @NoArgsConstructor @AllArgsConstructor @Builder
public class Session {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

   

    @ManyToOne(fetch=FetchType.LAZY)
    private School school;
    
    @ManyToOne(fetch = FetchType.LAZY)
    private CourseModule module;

    private LocalDate date;
    private LocalTime startTime;
    private LocalTime endTime;
    private boolean archived;
}

