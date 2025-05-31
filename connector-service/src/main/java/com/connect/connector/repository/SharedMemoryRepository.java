package com.connect.connector.repository;

import com.connect.connector.model.SharedMemory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SharedMemoryRepository extends JpaRepository<SharedMemory, UUID> {
}
