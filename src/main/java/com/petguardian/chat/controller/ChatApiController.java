package com.petguardian.chat.controller;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.petguardian.chat.dto.ChatMessageDTO;
import com.petguardian.chat.dto.ChatRoomDTO;
import com.petguardian.chat.dto.ReportRequestDTO;
import com.petguardian.chat.service.ChatService;
import com.petguardian.chat.service.chatmessage.report.ChatReportService;
import com.petguardian.chat.service.chatroom.ChatRoomService;
import com.petguardian.common.service.AuthStrategyService;

import org.springframework.web.bind.annotation.RequestBody;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * REST Controller for Chat Resource Management.
 * 
 * Responsibilities:
 * Provides JSON API for chatroom resolution
 * Serves lazy-loaded message history via AJAX
 * Enforces security and validation for data access
 * 
 * Uses dual-facade pattern:
 * {@link ChatRoomService} for chatroom operations
 * {@link ChatService} for message operations
 */
@RestController
@RequestMapping("/api/chatrooms")
@RequiredArgsConstructor
public class ChatApiController {

    // =========================================================================
    // DEPENDENCIES
    // =========================================================================

    private final AuthStrategyService authStrategyService;
    private final ChatService chatService;
    private final ChatRoomService chatRoomService;
    private final ChatReportService chatReportService;

    // =========================================================================
    // ENDPOINTS
    // =========================================================================

    /**
     * Finds or creates a chatroom with a specific partner.
     * 
     * @param partnerId    Target User ID
     * @param chatroomType Room Type (Default: 0 for Service)
     * @return ChatRoomDTO
     */
    @GetMapping
    public ResponseEntity<ChatRoomDTO> findChatroom(
            HttpServletRequest request,
            @RequestParam Integer partnerId,
            @RequestParam(required = false, defaultValue = "0") Integer chatroomType) {

        Integer currentUserId = authStrategyService.getCurrentUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.status(401).build();
        }

        // Use ChatRoomService facade for chatroom operations
        ChatRoomDTO chatroom = chatRoomService.findOrCreateChatroom(
                currentUserId, partnerId, chatroomType);

        return ResponseEntity.ok(chatroom);
    }

    /**
     * Retrieves paginated message history for a chatroom.
     * 
     * @param chatroomId Target Room ID
     * @param page       Page Index
     * @param size       Page Size
     * @return List of Message DTOs
     */
    @GetMapping("/{chatroomId}/messages")
    public ResponseEntity<List<ChatMessageDTO>> getMessages(
            HttpServletRequest request,
            @PathVariable Integer chatroomId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        Integer currentUserId = authStrategyService.getCurrentUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            List<ChatMessageDTO> dtos = chatService.getChatHistory(chatroomId, currentUserId, page, size);
            return ResponseEntity.ok(dtos);
        } catch (SecurityException e) {
            return ResponseEntity.status(403).build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    /**
     * Checks global unread status for the current user.
     * Used for the global header "Red Dot".
     */
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Boolean>> checkUnreadStatus(HttpServletRequest request) {
        Integer currentUserId = authStrategyService.getCurrentUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.status(401).build();
        }

        boolean hasUnread = chatService.hasUnreadMessages(currentUserId);
        return ResponseEntity.ok(Collections.singletonMap("hasUnread", hasUnread));
    }

    /**
     * Marks a room as read.
     * Called when opening a chat or focusing on the window.
     * Returns the UPDATED global unread status.
     */
    @PostMapping("/{chatroomId}/read")
    public ResponseEntity<Map<String, Boolean>> markAsRead(
            HttpServletRequest request,
            @PathVariable Integer chatroomId) {

        Integer currentUserId = authStrategyService.getCurrentUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.status(401).build();
        }

        chatService.markRoomAsRead(chatroomId, currentUserId);

        // Return new global status
        boolean hasUnread = chatService.hasUnreadMessages(currentUserId);
        return ResponseEntity.ok(Collections.singletonMap("hasUnread", hasUnread));
    }

    /**
     * Submits a report for a chat message.
     */
    @PostMapping("/report")
    public ResponseEntity<Void> submitReport(
            HttpServletRequest request,
            @RequestBody ReportRequestDTO reportRequest) {

        Integer currentUserId = authStrategyService.getCurrentUserId(request);
        if (currentUserId == null) {
            return ResponseEntity.status(401).build();
        }

        try {
            chatReportService.submitReport(
                    currentUserId,
                    reportRequest.getMessageId(),
                    reportRequest.getType(),
                    reportRequest.getReason());
            return ResponseEntity.ok().build();
        } catch (IllegalStateException e) {
            // Already reported
            return ResponseEntity.status(409).build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
