package com.packclaw.model.po;

import lombok.Data;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Session chat state entity
 */
@Data
public class SessionChat implements Serializable {

    private static final long serialVersionUID = 1L;

    private String sessionId;
    private String stateKey;
    private Integer itemIndex;
    private String stateData;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
