package com.petguardian.chat.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.petguardian.chat.model.ChatMemberDTO;
import com.petguardian.chat.model.ChatRoomDTO;
import com.petguardian.common.service.AuthStrategyService;
import com.petguardian.chat.service.chatmessage.ChatPageService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * MVC Controller for Chat View Rendering.
 * 
 * Responsibilities:
 * - Serves the initial HTML page (SSR with Thymeleaf)
 * - Pre-loads essential context (Current User, Contact List, Message Previews)
 * - Delegates authentication checks to strategy
 */
@Controller
public class ChatPageController {

    // ============================================================
    // DEPENDENCIES
    // ============================================================
    private final ChatPageService chatPageService;
    private final AuthStrategyService authStrategyService;

    public ChatPageController(ChatPageService chatPageService, AuthStrategyService authStrategyService) {
        this.chatPageService = chatPageService;
        this.authStrategyService = authStrategyService;
    }

    // ============================================================
    // VIEW ENDPOINTS
    // ============================================================

    /**
     * Entry Point: Chat MVP Interface.
     * 
     * Endpoint: /chat
     * Methods: GET, POST
     * 
     * Orchestrates the initial state required for the chat application.
     * Supports POST requests to allow secure transmission of 'sessionId' (hidden
     * from URL).
     * 
     * 1. Validates User Session (Redirect/Error if invalid)
     * 2. Loads Current User Profile
     * 3. Fetches Directory of Contacts
     * 4. Previews Latest Messages for Sidebar
     */
    @RequestMapping(value = "/chat", method = { RequestMethod.GET, RequestMethod.POST })
    public String chatMvpPage(HttpServletRequest request, Model model) {
        Integer userId = authStrategyService.getCurrentUserId(request);
        String userName = authStrategyService.getCurrentUserName(request);

        // Security Check
        if (userId == null) {
            model.addAttribute("error", "請先登入");
            return "frontend/chat/chat-mvp";
        }

        // Context Preparation

        // 1. Current User
        ChatMemberDTO currentUser = chatPageService.getMember(userId);
        if (currentUser == null) {
            // Fallback for transient auth states
            currentUser = new ChatMemberDTO(userId, userName != null ? userName : "User " + userId);
        }

        // 2. Chatroom List (Sidebar)
        List<ChatRoomDTO> chatrooms = chatPageService.getMyChatrooms(userId);

        // View Model Population
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("chatrooms", chatrooms);
        // Note: Sidebar data consolidated in ChatRoomDTO (via getMyChatrooms)

        return "frontend/chat/chat-mvp";
    }
}
