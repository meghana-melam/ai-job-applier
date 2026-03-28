package com.jobapplier.controller;

import com.jobapplier.service.JobAnalysisService;
import com.jobapplier.service.ReportService;
import com.jobapplier.dto.JobAnalysisResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import com.jobapplier.config.TelegramBotConfig;

@Slf4j
@Component
@RequiredArgsConstructor
public class BotController extends TelegramLongPollingBot {
    
    private final TelegramBotConfig botConfig;
    private final ReportService reportService;
    private final JobAnalysisService jobAnalysisService;
    
    @Override
    public String getBotUsername() {
        return botConfig.getBotUsername();
    }
    
    @Override
    public String getBotToken() {
        return botConfig.getBotToken();
    }
    
    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();
            
            String response = handleCommand(messageText, chatId);
            sendMessage(chatId, response);
        }
    }
    
    /**
     * Handle bot commands
     */
    private String handleCommand(String command, long chatId) {
        // Check if message contains a URL (job link)
        if (command.contains("http://") || command.contains("https://")) {
            return analyzeJobFromUrl(command);
        }
        
        return switch (command.toLowerCase()) {
            case "/start" -> "👋 Welcome to AI Job Applier!\n\n" +
                    "Available commands:\n" +
                    "/daily - Get daily report\n" +
                    "/weekly - Get weekly report\n" +
                    "📎 Paste any job URL - Get instant AI analysis\n" +
                    "/help - Show this help message";
            
            case "/daily" -> reportService.generateDailyReport();
            
            case "/weekly" -> reportService.generateWeeklyReport();
            
            case "/help" -> "🤖 AI Job Applier Commands:\n\n" +
                    "/daily - View today's job application activity\n" +
                    "/weekly - View this week's summary\n" +
                    "📎 Paste job URL - Instant AI match analysis\n" +
                    "/start - Welcome message\n\n" +
                    "💡 Tip: Just paste any job link (LinkedIn, Naukri, etc.) " +
                    "and I'll analyze if it matches your resume!";
            
            default -> "❓ Unknown command. Type /help to see available commands.\n\n" +
                    "💡 Or paste a job URL to analyze it!";
        };
    }
    
    /**
     * Analyze job from URL and format response for Telegram
     */
    private String analyzeJobFromUrl(String message) {
        try {
            // Extract URL from message
            String url = message.trim();
            if (message.contains(" ")) {
                // If message has multiple words, extract the URL
                String[] parts = message.split("\\s+");
                for (String part : parts) {
                    if (part.startsWith("http")) {
                        url = part;
                        break;
                    }
                }
            }
            
            log.info("Analyzing job from Telegram: {}", url);
            sendMessage(Long.parseLong(botConfig.getChatId()), "🔍 Analyzing job... Please wait...");
            
            JobAnalysisResponse analysis = jobAnalysisService.analyzeJobUrl(url, true);
            
            return formatAnalysisForTelegram(analysis);
            
        } catch (Exception e) {
            log.error("Error analyzing job from Telegram", e);
            return "❌ Error analyzing job: " + e.getMessage() + 
                   "\n\nPlease make sure you pasted a valid job URL.";
        }
    }
    
    /**
     * Format job analysis response for Telegram
     */
    private String formatAnalysisForTelegram(JobAnalysisResponse response) {
        StringBuilder message = new StringBuilder();
        
        // Header with match score
        message.append("🎯 JOB ANALYSIS RESULT\n");
        message.append("━━━━━━━━━━━━━━━━━━━━\n\n");
        
        // Job details
        if (response.getTitle() != null && !response.getTitle().isEmpty()) {
            message.append("📋 Position: ").append(response.getTitle()).append("\n");
        }
        if (response.getCompany() != null && !response.getCompany().isEmpty()) {
            message.append("🏢 Company: ").append(response.getCompany()).append("\n");
        }
        if (response.getLocation() != null && !response.getLocation().isEmpty()) {
            message.append("📍 Location: ").append(response.getLocation()).append("\n");
        }
        message.append("\n");
        
        // Match score with emoji
        String scoreEmoji = response.getMatchScore() >= 70 ? "🟢" : 
                           response.getMatchScore() >= 60 ? "🟡" : "🔴";
        message.append(scoreEmoji).append(" Match Score: ")
               .append(String.format("%.0f%%", response.getMatchScore())).append("\n");
        message.append("📊 Level: ").append(response.getMatchLevel()).append("\n");
        message.append("💡 Recommendation: ").append(response.getRecommendation()).append("\n\n");
        
        // Skills analysis
        if (response.getMatchingSkills() != null && !response.getMatchingSkills().isEmpty()) {
            message.append("✅ Your Matching Skills (")
                   .append(response.getMatchingSkills().size()).append("):\n");
            response.getMatchingSkills().stream()
                   .limit(8)
                   .forEach(skill -> message.append("  • ").append(skill).append("\n"));
            if (response.getMatchingSkills().size() > 8) {
                message.append("  ... and ")
                       .append(response.getMatchingSkills().size() - 8)
                       .append(" more\n");
            }
            message.append("\n");
        }
        
        if (response.getMissingSkills() != null && !response.getMissingSkills().isEmpty()) {
            message.append("❌ Missing Skills (")
                   .append(response.getMissingSkills().size()).append("):\n");
            response.getMissingSkills().stream()
                   .limit(8)
                   .forEach(skill -> message.append("  • ").append(skill).append("\n"));
            if (response.getMissingSkills().size() > 8) {
                message.append("  ... and ")
                       .append(response.getMissingSkills().size() - 8)
                       .append(" more\n");
            }
            message.append("\n");
        }
        
        // Why good fit
        if (response.getWhyGoodFit() != null && !response.getWhyGoodFit().isEmpty()) {
            message.append("💬 Analysis:\n").append(response.getWhyGoodFit()).append("\n\n");
        }
        
        // Save status
        if (response.isSaved()) {
            message.append("✅ Job saved to database (ID: ")
                   .append(response.getJobId()).append(")\n");
        } else {
            message.append("ℹ️ Job not saved (score below 60%)\n");
        }
        
        // Cover letter availability
        if (response.getCoverLetter() != null && !response.getCoverLetter().contains("too low")) {
            message.append("\n📧 Cover letter generated! Check API for full details.\n");
        }
        
        return message.toString();
    }
    
    /**
     * Send message to Telegram
     */
    public void sendMessage(long chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(text);
        
        try {
            execute(message);
            log.info("Message sent to chat: {}", chatId);
        } catch (TelegramApiException e) {
            log.error("Error sending message", e);
        }
    }
    
    /**
     * Send daily report to configured chat
     */
    public void sendDailyReport() {
        try {
            String report = reportService.generateDailyReport();
            long chatId = Long.parseLong(botConfig.getChatId());
            sendMessage(chatId, report);
            log.info("Daily report sent successfully");
        } catch (Exception e) {
            log.error("Error sending daily report", e);
        }
    }
}
