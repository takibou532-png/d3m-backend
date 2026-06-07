package com.menu.demo.Repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.menu.demo.Models.SuperAdminAction;
import com.menu.demo.Models.User;

@Repository
public interface SuperAdminActionRepository extends JpaRepository<SuperAdminAction, Long> {

    List<SuperAdminAction> findTop20ByOrderByPerformedAtDesc();

    List<SuperAdminAction> findAllBySuperAdmin(User superAdmin);
}
