package com.jobapplier.service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AIService {
    
    @Value("${gemini.api.key}")
    private String geminiApiKey;
    
    @Value("${gemini.model:gemini-pro}")
    private String model;
    
    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");
    private final OkHttpClient client = new OkHttpClient();
    private final Gson gson = new Gson();
    
    /**
     * Test Gemini API connection
     */
    public boolean testConnection() {
        try {
            String url = String.format(
                "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent?key=%s",
                model, geminiApiKey
            );
            
            JsonObject requestBody = new JsonObject();
            JsonArray contents = new JsonArray();
            JsonObject content = new JsonObject();
            JsonArray parts = new JsonArray();
            JsonObject part = new JsonObject();
            
            part.addProperty("text", "Hello, test connection");
            parts.add(part);
            content.add("parts", parts);
            contents.add(content);
            requestBody.add("contents", contents);
            
            RequestBody body = RequestBody.create(gson.toJson(requestBody), JSON);
            Request request = new Request.Builder()
                    .url(url)
                    .post(body)
                    .build();
            
            try (Response response = client.newCall(request).execute()) {
                if (response.isSuccessful()) {
                    log.info("Gemini API connection successful");
                    return true;
                }
            }
            return false;
        } catch (Exception e) {
            log.error("Gemini API connection failed", e);
            return false;
        }
    }
    
    /**
     * Get AI configuration info
     */
    public String getModelInfo() {
        return String.format("Using Google Gemini model: %s", model);
    }
}
