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
import java.util.stream.Collectors;

import com.google.cloud.vertexai.VertexAI;
import com.google.cloud.vertexai.api.GenerateContentResponse;
import com.google.cloud.vertexai.generativeai.GenerativeModel;
import com.google.cloud.vertexai.generativeai.ResponseHandler;

@RestController
@RequestMapping("/api/ai")
@CrossOrigin(origins = "*") // In production, restrict this to specific origins
@Tag(name = "AI Assistant", description = "Vertex AI powered natural language queries")
public class AiAssistantController {

    private static final Logger logger = LoggerFactory.getLogger(AiAssistantController.class);

    @Value("${smartstadium.vertexai.mock:true}")
    private boolean useMockResponse;

    @Value("${GCP_PROJECT_ID:promptwars-493516}")
    private String projectId;

    @Value("${GCP_LOCATION:us-central1}")
    private String location;

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
            responseMessage = callVertexAI(message);
        }

        return ResponseEntity.ok(Map.of(
                "response", responseMessage,
                "role", "assistant"
        ));
    }

    private String callVertexAI(String userMessage) {
        try (VertexAI vertexAI = new VertexAI(projectId, location)) {
            GenerativeModel model = new GenerativeModel("gemini-2.5-flash", vertexAI);

            // Context Injection: Give the AI the current live data
            String stadiumContext = crowdService.getAllDensities().stream()
                    .map(d -> String.format("- %s: %d/%d people", d.displayName(), d.currentCount(), d.capacity()))
                    .collect(Collectors.joining("\n"));

            String prompt = String.format("""
                You are the Smart Stadium AI Assistant. 
                Here is the current live state of the stadium:
                %s
                
                A fan is asking: "%s"
                
                Provide a helpful, friendly, and concise response based purely on the data above. 
                Suggest the best places to go to avoid crowds.
                """, stadiumContext, userMessage);

            GenerateContentResponse response = model.generateContent(prompt);
            return ResponseHandler.getText(response);
        } catch (Exception e) {
            logger.error("Error calling Vertex AI: ", e);
            return "I'm having trouble reaching my AI brain right now, but according to my backup sensors, you should check the North Concourse for lower crowds!";
        }
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
