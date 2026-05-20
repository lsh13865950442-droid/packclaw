package com.packclaw.controller;

import com.alibaba.fastjson2.JSON;
import com.packclaw.common.domain.Response;
import com.packclaw.common.exception.ServiceException;
import com.packclaw.manager.HarnessAgentFactory;
import com.packclaw.model.req.ChatRequest;
import com.packclaw.model.req.SessionRequest;
import com.packclaw.service.ChatService;
import io.agentscope.core.agent.Event;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.http.codec.ServerSentEvent;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.time.Duration;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeoutException;

@Slf4j
@RestController
@RequestMapping("/chat")
@Tag(name = "AI Chat", description = "AI assistant streaming chat")
public class ChatController {

    @Resource
    private ChatService chatService;

    @Resource
    private HarnessAgentFactory harnessAgentFactory;

    @PostMapping("/session")
    @Operation(summary = "Create session", description = "Create a new chat session")
    public Response<String> conversation(@RequestBody SessionRequest request,
                                         HttpServletRequest httpRequest) {
        if (request.getMessage() == null || request.getMessage().trim().isEmpty()) {
            log.warn("message is empty");
            throw new ServiceException("Please enter a valid question");
        }
        String ip = httpRequest.getRemoteAddr();
        String sessionId = chatService.getSessionId(ip, request.getMessage());
        log.info("Session created, sessionId: {}", sessionId);
        return Response.ok(sessionId);
    }

    @PostMapping("/generate-title")
    @Operation(summary = "Generate session title", description = "Generate title for a session")
    public Response<String> generateSessionTitle(@RequestParam(value = "sessionId") @Parameter(description = "Session ID") String sessionId) {
        String name = chatService.generateSessionTitle(sessionId);
        log.info("Session title generated, sessionId: {}, name: {}", sessionId, name);
        return Response.ok(name);
    }

    @PostMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> chat(@RequestBody ChatRequest request,
                                              HttpServletRequest httpRequest) {
        log.info("Received user query: {}", request.getQuery());

        try {
            Sinks.Many<ServerSentEvent<String>> sink =
                    Sinks.many().unicast().onBackpressureBuffer();

            sink.tryEmitNext(ServerSentEvent.builder("[START]").build());

            String query = request.getQuery();
            String sessionId = request.getSessionId();

            processStream(harnessAgentFactory.stream(sessionId, query), sink, query, sessionId);

            return sink.asFlux()
                    .timeout(Duration.ofMinutes(30), Flux.error(new TimeoutException("Response timeout")))
                    .doOnError(e -> log.error("Error occurred during streaming", e));
        } catch (Exception e) {
            log.error("Failed to process user query: {}", request.getQuery(), e);
            Map<String, Object> errorMap = new HashMap<>();
            errorMap.put("type", "error");
            errorMap.put("content", "Sorry, the agent encountered an error. Please try again.");
            errorMap.put("timestamp", LocalTime.now().toString());
            return Flux.just(ServerSentEvent.builder(JSON.toJSONString(errorMap)).build());
        }
    }

    public void processStream(Flux<Event> generator, Sinks.Many<ServerSentEvent<String>> sink, String query, String sessionId) {
        generator
                .doOnNext(event -> {
                    // 所有事件都发送
                    sink.tryEmitNext(ServerSentEvent.builder(JSON.toJSONString(event)).build());
                })
                .doOnError(e -> log.error("Unexpected error in stream processing: {}", e.getMessage(), e))
                .doOnComplete(() -> {
                    log.info("Stream completed, emitting [DONE]");
                    sink.tryEmitNext(ServerSentEvent.builder("[DONE]").build());
                    sink.tryEmitComplete();
                })
                .doFinally(signalType -> {
                    harnessAgentFactory.deleteAgent(sessionId);
                    log.info("Removed session {} and deleted agent, signal: {}", sessionId, signalType);
                })
                .subscribe(
                        null,
                        e -> {
                            log.error("Agent call failed, query: {}, error: {}", query, e.getMessage(), e);
                            Map<String, Object> errorMap = new HashMap<>();
                            errorMap.put("type", "error");
                            errorMap.put("content", "Sorry, the agent encountered an error. Please try again.");
                            errorMap.put("timestamp", LocalTime.now().toString());
                            sink.tryEmitNext(ServerSentEvent.builder(JSON.toJSONString(errorMap)).build());
                            sink.tryEmitNext(ServerSentEvent.builder("[DONE]").build());
                            sink.tryEmitComplete();
                        });

        log.info("Added session {} to active subscriptions", sessionId);
    }

    @PostMapping("/stop")
    @Operation(summary = "Stop session", description = "Stop an active AI chat session")
    public Response<String> stop(@RequestParam(value = "sessionId") @Parameter(description = "Session ID") String sessionId) {
        log.info("Received stop request for sessionId: {}", sessionId);

        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new ServiceException("Please provide a valid session ID");
        }

        harnessAgentFactory.interruptAgent(sessionId);
        log.info("Successfully interrupted agent for session: {}", sessionId);
        return Response.ok("Session stopped");
    }

    @PostMapping("/next-suggestions")
    @Operation(summary = "Get follow-up suggestions", description = "Generate 3 follow-up suggestions based on chat history")
    public Response<java.util.List<String>> getNextSuggestions(@RequestParam(value = "sessionId") @Parameter(description = "Session ID") String sessionId) {
        log.info("Received next suggestions request for sessionId: {}", sessionId);

        if (sessionId == null || sessionId.trim().isEmpty()) {
            throw new ServiceException("Please provide a valid session ID");
        }
        java.util.List<String> suggestions = chatService.generateNextSuggestions(sessionId);
        log.info("Successfully generated {} suggestions for session: {}", suggestions.size(), sessionId);
        return Response.ok(suggestions);
    }
}
