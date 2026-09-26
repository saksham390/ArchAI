package com.archai.design.repository;

import com.archai.design.entity.SystemDesign;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SystemDesignRepository extends JpaRepository<SystemDesign, Long> {
    List<SystemDesign> findAllByOwnerIdOrderByUpdatedAtDesc(String ownerId);
    Optional<SystemDesign> findByIdAndOwnerId(Long id, String ownerId);
}
