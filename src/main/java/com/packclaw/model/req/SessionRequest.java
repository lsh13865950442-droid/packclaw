package com.packclaw.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/**
 * Session request DTO
 */
@Data
@Schema(description = "Session request parameters")
public class SessionRequest {

    @NotBlank(message = "Message cannot be empty")
    @Schema(description = "User message", required = true, example = "Hello, introduce yourself")
    private String message;
}
