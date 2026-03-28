package com.jobapplier.repository;

import com.jobapplier.model.Application;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ApplicationRepository extends JpaRepository<Application, Long> {
    
    List<Application> findByStatus(String status);
    
    long countByStatus(String status);
    
    @Query("SELECT a FROM Application a WHERE a.job.id = :jobId")
    List<Application> findByJobId(@Param("jobId") Long jobId);
    
    @Query("SELECT a FROM Application a WHERE a.appliedDate >= :since ORDER BY a.appliedDate DESC")
    List<Application> findApplicationsSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(a) FROM Application a WHERE a.status = 'SUCCESS' AND a.appliedDate >= :since")
    Long countSuccessfulApplicationsSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(a) FROM Application a WHERE a.appliedDate >= :since")
    Long countApplicationsSince(@Param("since") LocalDateTime since);
    
    @Query("SELECT a FROM Application a WHERE a.responseReceived = false AND a.status = 'SUCCESS'")
    List<Application> findPendingResponses();
}
