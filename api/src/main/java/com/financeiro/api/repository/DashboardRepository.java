package com.financeiro.api.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.financeiro.api.domain.Dashboard;
import com.financeiro.api.domain.User;
import com.financeiro.api.domain.enums.Status;

@Repository
public interface DashboardRepository extends JpaRepository<Dashboard, UUID>{
    List<Dashboard> findAllByStatusInAndUser(List<Status> status, User user);
}
