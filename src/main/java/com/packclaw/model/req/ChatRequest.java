package com.packclaw.model.req;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.List;

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

    @Schema(description = "Media content list (Base64 encoded)")
    private List<MediaItem> mediaItems;

    /**
     * 多媒体内容块 DTO
     */
    @Data
    public static class MediaItem {
        @Schema(description = "MIME type, e.g. image/png, audio/mp3, video/mp4")
        private String mediaType;

        @Schema(description = "Server-side absolute file path returned by /file/upload")
        private String filePath;

        @Schema(description = "Original file name")
        private String name;
    }
}
