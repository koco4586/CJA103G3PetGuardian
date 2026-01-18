package com.petguardian.chat.controller;

import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.petguardian.chat.model.ChatMemberDTO;
import com.petguardian.chat.service.AuthStrategyService;
import com.petguardian.chat.service.ChatPageService;

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
     * Orchestrates the initial state required for the chat application:
     * 1. Validates User Session (Redirect/Error if invalid)
     * 2. Loads Current User Profile
     * 3. Fetches Directory of Contacts
     * 4. Previews Latest Messages for Sidebar
     */
    @GetMapping("/chat/mvp")
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

        // 2. Contact List
        List<ChatMemberDTO> allMembers = chatPageService.getAllMembers();

        // 3. Sidebar Previews
        Map<Integer, String> lastMessages = chatPageService.getLastMessages(userId);

        // View Model Population
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("users", allMembers);
        model.addAttribute("lastMessages", lastMessages);

        return "frontend/chat/chat-mvp";
    }
}
