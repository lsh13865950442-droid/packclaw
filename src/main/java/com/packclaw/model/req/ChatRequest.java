package com.packclaw.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Chat request DTO
 */
@Data
@Schema(description = "Chat request parameters")
public class ChatRequest {

    @NotBlank(message = "Query cannot be empty")
    @Schema(description = "User query", required = true, example = "Hello, introduce yourself")
    private String query;

    @NotBlank(message = "Session ID cannot be empty")
    @Schema(description = "Session ID", required = true, example = "1234567890")
    private String sessionId;
}
