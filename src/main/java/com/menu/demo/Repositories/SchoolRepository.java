package com.menu.demo.Repositories;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.menu.demo.Enums.SubscriptionStatus;
import com.menu.demo.Models.School;
@Repository
public interface SchoolRepository extends JpaRepository <School , Long> {
	   List<School> findAllBySubscriptionStatus(SubscriptionStatus status);

	    Page<School> findAllBySubscriptionStatusNot(SubscriptionStatus status, Pageable pageable);

	    // Schools whose subscription expires today (for expiry scheduler)
	    @Query("SELECT s FROM School s WHERE s.subscriptionExpiresAt = :today AND s.subscriptionStatus = 'ACTIVE'")
	    List<School> findAllExpiringToday(@Param("today") LocalDate today);
}
