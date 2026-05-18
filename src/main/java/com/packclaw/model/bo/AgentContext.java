package com.packclaw.model.bo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Agent context for tool execution
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AgentContext {

    private String sessionId;
}
