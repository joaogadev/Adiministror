package com.backend.adiministror.repository;

import com.backend.adiministror.model.TenantModel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface TenantRepository extends JpaRepository<TenantModel, UUID> {
}
