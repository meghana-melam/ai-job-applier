package com.jobapplier;

import com.jobapplier.controller.BotController;
import com.jobapplier.service.ResumeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.File;

@Slf4j
@SpringBootApplication
@RequiredArgsConstructor
public class AiJobApplierApplication {
    
    private final TelegramBotsApi telegramBotsApi;
    private final BotController botController;
    private final ResumeService resumeService;
    
    @Value("${resume.directory}")
    private String resumeDirectory;
    
    public static void main(String[] args) {
        SpringApplication.run(AiJobApplierApplication.class, args);
    }
    
    /**
     * Register Telegram bot on startup
     */
    @Bean
    public CommandLineRunner initBot() {
        return args -> {
            try {
                telegramBotsApi.registerBot(botController);
                log.info("Telegram bot registered successfully");
            } catch (TelegramApiException e) {
                log.error("Failed to register Telegram bot", e);
            }
        };
    }
    
    /**
     * Startup banner
     */
    @Bean
    public CommandLineRunner startup() {
        return args -> {
            log.info("===========================================");
            log.info("   AI Job Applier - Started Successfully   ");
            log.info("===========================================");
            log.info("API: http://localhost:8080");
            log.info("Telegram Bot: Active");
            log.info("Schedulers: Enabled");
            log.info("===========================================");
            
            // Parse resume on startup if not already parsed
            try {
                if (resumeService.getActiveResume() == null) {
                    File dir = new File(resumeDirectory);
                    File[] pdfFiles = dir.listFiles((d, name) -> name.toLowerCase().endsWith(".pdf"));
                    
                    if (pdfFiles != null && pdfFiles.length > 0) {
                        String resumePath = pdfFiles[0].getAbsolutePath();
                        log.info("Parsing resume on startup: {}", pdfFiles[0].getName());
                        resumeService.parseAndSaveResume(resumePath);
                        log.info("Resume parsed and activated successfully");
                    } else {
                        log.warn("No resume found in: {}", resumeDirectory);
                    }
                } else {
                    log.info("Active resume already exists");
                }
            } catch (Exception e) {
                log.error("Error parsing resume on startup", e);
            }
        };
    }
}
