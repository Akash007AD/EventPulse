package com.eventpulse.ai.controller;

import com.eventpulse.ai.dto.AiChatRequest;
import com.eventpulse.ai.dto.AiChatResponse;
import com.eventpulse.ai.dto.FaqItemDto;
import com.eventpulse.ai.service.AiDiscoveryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiDiscoveryController {

    private final AiDiscoveryService aiDiscoveryService;

    @PostMapping("/chat")
    public ResponseEntity<AiChatResponse> chat(@Valid @RequestBody AiChatRequest request) {
        log.debug("Received AI conversational chat request: {}", request.getQuery());
        AiChatResponse response = aiDiscoveryService.handleChat(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/discovery/search")
    public ResponseEntity<AiChatResponse> searchEvents(@Valid @RequestBody AiChatRequest request) {
        log.debug("Received AI event discovery search request: {}", request.getQuery());
        AiChatResponse response = aiDiscoveryService.handleChat(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/faq/topics")
    public ResponseEntity<List<FaqItemDto>> getFaqTopics() {
        log.debug("Fetching platform FAQ topics");
        List<FaqItemDto> faqs = aiDiscoveryService.getFaqTopics();
        return ResponseEntity.ok(faqs);
    }
}
