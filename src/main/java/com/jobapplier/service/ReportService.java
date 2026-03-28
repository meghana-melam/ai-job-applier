package com.jobapplier.service;

import com.jobapplier.model.Application;
import com.jobapplier.model.Job;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ReportService {
    
    private final JobService jobService;
    private final ApplyService applyService;
    
    /**
     * Generate daily report
     */
    public String generateDailyReport() {
        log.info("Generating daily report");
        
        LocalDateTime today = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        // Get stats
        JobService.JobStats jobStats = jobService.getStats(1);
        ApplyService.ApplicationStats appStats = applyService.getStats(1);
        
        // Get recent applications
        List<Application> recentApplications = applyService.getRecentApplications(1);
        
        StringBuilder report = new StringBuilder();
        report.append("📊 **AI Job Applier - Daily Report**\n");
        report.append(String.format("Date: %s\n\n", today.format(formatter)));
        
        report.append("**Jobs Summary:**\n");
        report.append(String.format("• Total jobs found: %d\n", jobStats.total()));
        report.append(String.format("• Jobs matched: %d\n", jobStats.matched()));
        report.append(String.format("• Jobs applied: %d\n\n", jobStats.applied()));
        
        report.append("**Applications Summary:**\n");
        report.append(String.format("• Total applications: %d\n", appStats.total()));
        report.append(String.format("• Successful: %d\n", appStats.successful()));
        report.append(String.format("• Pending responses: %d\n\n", appStats.pending()));
        
        if (!recentApplications.isEmpty()) {
            report.append("**Recent Applications:**\n");
            int count = 0;
            for (Application app : recentApplications) {
                if (count >= 5) break; // Limit to top 5
                Job job = app.getJob();
                report.append(String.format("• %s at %s (Score: %.0f%%)\n", 
                        job.getTitle(), job.getCompany(), job.getMatchScore()));
                count++;
            }
        }
        
        report.append("\n✅ Keep up the great work!");
        
        return report.toString();
    }
    
    /**
     * Generate weekly report
     */
    public String generateWeeklyReport() {
        log.info("Generating weekly report");
        
        JobService.JobStats jobStats = jobService.getStats(7);
        ApplyService.ApplicationStats appStats = applyService.getStats(7);
        
        List<Application> weekApplications = applyService.getRecentApplications(7);
        
        StringBuilder report = new StringBuilder();
        report.append("📈 **AI Job Applier - Weekly Report**\n\n");
        
        report.append("**This Week's Performance:**\n");
        report.append(String.format("• Jobs found: %d\n", jobStats.total()));
        report.append(String.format("• Jobs matched: %d\n", jobStats.matched()));
        report.append(String.format("• Applications sent: %d\n", appStats.total()));
        report.append(String.format("• Success rate: %.1f%%\n\n", 
                calculateSuccessRate(appStats.total(), appStats.successful())));
        
        report.append("**Top Matches:**\n");
        weekApplications.stream()
                .sorted((a, b) -> Double.compare(b.getJob().getMatchScore(), a.getJob().getMatchScore()))
                .limit(10)
                .forEach(app -> {
                    Job job = app.getJob();
                    report.append(String.format("• %s at %s (%.0f%%)\n", 
                            job.getTitle(), job.getCompany(), job.getMatchScore()));
                });
        
        return report.toString();
    }
    
    /**
     * Calculate success rate
     */
    private double calculateSuccessRate(Long total, Long successful) {
        if (total == 0) return 0.0;
        return (successful.doubleValue() / total.doubleValue()) * 100;
    }
}
