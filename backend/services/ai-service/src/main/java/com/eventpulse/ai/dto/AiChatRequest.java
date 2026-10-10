package com.eventpulse.ai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiChatRequest {

    @NotBlank(message = "Query must not be blank")
    @Size(max = 500, message = "Query must not exceed 500 characters")
    private String query;

    private String conversationId;

    @Builder.Default
    private Integer maxEvents = 5;
}
