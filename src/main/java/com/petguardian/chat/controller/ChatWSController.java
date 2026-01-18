package com.petguardian.chat.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import com.petguardian.chat.model.ChatMessageDTO;
import com.petguardian.chat.service.ChatService;

/**
 * WebSocket controller for real-time chat messages.
 */
@Controller
public class ChatWSController {

    private final ChatService chatService;
    private final SimpMessagingTemplate messagingTemplate;

    public ChatWSController(ChatService chatService, SimpMessagingTemplate messagingTemplate) {
        this.chatService = chatService;
        this.messagingTemplate = messagingTemplate;
    }

    /**
     * Handle incoming chat message via WebSocket.
     * Persists message and broadcasts to both sender and receiver.
     */
    @MessageMapping("/chat.send")
    public void sendMessage(@Payload ChatMessageDTO dto) {
        // Delegate to service to handle everything (persist, reply info, etc.)
        ChatMessageDTO responseDto = chatService.handleIncomingMessage(dto);

        // Send to receiver's personal topic
        messagingTemplate.convertAndSend(
                "/topic/messages." + dto.getReceiverId(),
                responseDto);

        // Also send to sender's personal topic (for UI confirmation)
        messagingTemplate.convertAndSend(
                "/topic/messages." + dto.getSenderId(),
                responseDto);
    }
}
