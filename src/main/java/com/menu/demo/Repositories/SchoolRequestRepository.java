package com.menu.demo.Repositories;


import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.menu.demo.Enums.RequestStatus;
import com.menu.demo.Models.SchoolRequest;

@Repository
public interface SchoolRequestRepository extends JpaRepository <SchoolRequest ,Long> {
	List<SchoolRequest> findAllByStatusOrderByCreatedAtDesc(RequestStatus status);

    Page<SchoolRequest> findAllByStatus(RequestStatus status, Pageable pageable);

    boolean existsByEmail(String email);
}
