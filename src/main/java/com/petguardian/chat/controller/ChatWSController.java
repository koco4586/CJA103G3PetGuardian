package com.petguardian.chat.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.petguardian.chat.dto.ChatMessageDTO;
import com.petguardian.chat.service.ChatService;

/**
 * WebSocket Controller for Real-Time Messaging.
 * 
 * Responsibilities:
 * - Handles STOMP message events
 * - Orchestrates message persistence via ChatService
 * - Broadcasts events to targeted user topics
 */
@Controller
public class ChatWSController {

    // ============================================================
    // DEPENDENCIES
    // ============================================================
    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWSController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    // ============================================================
    // HANDLERS
    // ============================================================

    /**
     * Processes incoming chat messages.
     * 
     * Flow:
     * 1. Persist message (Validation + DB Save)
     * 2. Enrich DTO (Generate ID + Timestamp)
     * 3. Publish to Receiver's Topic (Real-time update)
     * 4. Publish to Sender's Topic (Visual confirmation/"Sent" status)
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDTO dto) {
        // Delegate core logic to service layer
        ChatMessageDTO responseDto = chatService.handleIncomingMessage(dto);

        // Notify Receiver
        messagingTemplate.convertAndSend(
                "/topic/messages." + dto.getReceiverId(),
                responseDto);

        // Notify Sender (Echo)
        messagingTemplate.convertAndSend(
                "/topic/messages." + dto.getSenderId(),
                responseDto);
    }

    /**
     * Exception Handler for Validation Errors.
     * Captures IllegalArgumentException from Service layer (Fail Fast Strategy)
     * and sends feedback to the user.
     */
    @org.springframework.messaging.handler.annotation.MessageExceptionHandler
    @org.springframework.messaging.simp.annotation.SendToUser("/queue/errors")
    public String handleException(IllegalArgumentException e) {
        return "Error: " + e.getMessage();
    }
}
