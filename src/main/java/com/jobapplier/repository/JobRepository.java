package com.jobapplier.repository;

import com.jobapplier.model.Job;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface JobRepository extends JpaRepository<Job, Long> {
    
    Optional<Job> findByJobUrl(String jobUrl);
    
    List<Job> findByStatus(String status);
    
    List<Job> findBySourcePlatform(String sourcePlatform);
    
    @Query("SELECT j FROM Job j WHERE j.matchScore >= :minScore ORDER BY j.matchScore DESC")
    List<Job> findByMinMatchScore(@Param("minScore") Double minScore);
    
    @Query("SELECT j FROM Job j WHERE j.status = :status AND j.isEasyApply = true ORDER BY j.matchScore DESC")
    List<Job> findEasyApplyJobsByStatus(@Param("status") String status);
    
    @Query("SELECT j FROM Job j WHERE j.scrapedDate >= :since ORDER BY j.scrapedDate DESC")
    List<Job> findRecentJobs(@Param("since") LocalDateTime since);
    
    @Query("SELECT COUNT(j) FROM Job j WHERE j.status = :status AND j.scrapedDate >= :since")
    Long countByStatusSince(@Param("status") String status, @Param("since") LocalDateTime since);
    
    boolean existsByJobUrl(String jobUrl);
}
