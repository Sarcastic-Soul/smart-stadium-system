package com.smartstadium.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.smartstadium.service.CrowdService;
import com.smartstadium.model.Zone;
import com.smartstadium.model.CrowdData;

import java.util.Map;
import java.util.Optional;
import java.util.Comparator;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*") // In production, restrict this to specific origins
@Tag(name = "AI Assistant", description = "Vertex AI powered natural language queries")
public class AiAssistantController {

    private static final Logger logger = LoggerFactory.getLogger(AiAssistantController.class);

    @Value("${smartstadium.vertexai.mock:true}")
    private boolean useMockResponse;

    private final CrowdService crowdService;

    public AiAssistantController(CrowdService crowdService) {
        this.crowdService = crowdService;
    }

    @PostMapping("/chat")
    @Operation(summary = "Chat with AI", description = "Accepts natural language constraints and returns an AI-generated suggestion.")
    public ResponseEntity<Map<String, String>> chat(@RequestBody Map<String, String> request) {
        String message = request.getOrDefault("message", "").toLowerCase();
        logger.info("Received AI chat message: {}", message);

        String responseMessage;

        if (useMockResponse) {
            responseMessage = generateMockResponse(message);
        } else {
            // TODO: In a real environment with credentials, this would call Vertex AI Models
            // For example: vertexAiTemplate.generate(message);
            responseMessage = "[Vertex AI Live]: " + generateMockResponse(message);
        }

        return ResponseEntity.ok(Map.of(
                "response", responseMessage,
                "role", "assistant"
        ));
    }

    private String generateMockResponse(String message) {
        if (message.contains("restroom") || message.contains("bathroom") || message.contains("toilet")) {
            CrowdData north = crowdService.getCrowdData(Zone.RESTROOM_NORTH);
            CrowdData south = crowdService.getCrowdData(Zone.RESTROOM_SOUTH);
            
            if (north.getCurrentCount() < south.getCurrentCount()) {
                return "The North Restroom currently has fewer people (" + north.getCurrentCount() + ") compared to the South Restroom. I recommend heading there!";
            } else {
                return "The South Restroom currently has fewer people (" + south.getCurrentCount() + ") compared to the North Restroom. I recommend heading there!";
            }
        } 
        
        if (message.contains("food") || message.contains("hungry") || message.contains("eat")) {
            CrowdData east = crowdService.getCrowdData(Zone.FOOD_COURT_EAST);
            CrowdData west = crowdService.getCrowdData(Zone.FOOD_COURT_WEST);
            
            if (east.getCurrentCount() < west.getCurrentCount()) {
                 return "Food Court East is less crowded right now. I suggest going there for a quicker bite.";
            } else {
                 return "Food Court West is less crowded right now. I suggest going there for a quicker bite.";
            }
        }
        
        return "I am your Smart Stadium Assistant. You can ask me to find the nearest restrooms, least crowded food courts, or quick exit routes!";
    }
}
