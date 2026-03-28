package com.jobapplier.repository;

import com.jobapplier.model.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByIsActiveTrue();
    Optional<Resume> findFirstByIsActiveTrueOrderByCreatedAtDesc();
}
