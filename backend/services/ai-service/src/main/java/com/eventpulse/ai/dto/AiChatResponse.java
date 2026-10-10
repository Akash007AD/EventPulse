package com.eventpulse.ai.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatResponse {
    private String answer;
    private ChatIntent intent;
    private List<EventSummaryDto> events;
    private SearchCriteriaDto searchCriteria;
    private List<String> suggestedFollowUps;
    private boolean guardrailTriggered;
}
