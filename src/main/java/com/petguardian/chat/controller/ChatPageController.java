package com.petguardian.chat.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.petguardian.chat.model.ChatMemberVO;
import com.petguardian.chat.service.AuthStrategyService;
import com.petguardian.chat.service.ChatPageService;

import jakarta.servlet.http.HttpServletRequest;

/**
 * MVC Controller for Chat View Rendering.
 * Contextualizes the chat environment with user identity and initial state.
 */
@Controller
public class ChatPageController {

    private final ChatPageService chatPageService;
    private final AuthStrategyService authStrategyService;

    public ChatPageController(ChatPageService chatPageService, AuthStrategyService authStrategyService) {
        this.chatPageService = chatPageService;
        this.authStrategyService = authStrategyService;
    }

    /**
     * View Entry Point: Chat Interface.
     * Preloads user context, contact list, and conversation previews.
     */
    @GetMapping("/chat/mvp")
    public String chatMvpPage(HttpServletRequest request, Model model) {
        // Get current user via AuthStrategyService
        Integer userId = authStrategyService.getCurrentUserId(request);
        String userName = authStrategyService.getCurrentUserName(request);

        if (userId == null) {
            // Not authenticated - redirect or show error
            model.addAttribute("error", "請先登入");
            return "frontend/chat/chat-mvp";
        }

        // Build current user VO
        ChatMemberVO currentUser = chatPageService.getMember(userId);
        if (currentUser == null) {
            // Fallback: create from auth info
            currentUser = new ChatMemberVO();
            currentUser.setMemId(userId);
            currentUser.setMemName(userName != null ? userName : "User " + userId);
        }

        // Get all members for user list
        List<ChatMemberVO> allMembers = chatPageService.getAllMembers();

        // Get last messages for all contacts
        java.util.Map<Integer, String> lastMessages = chatPageService.getLastMessages(userId);

        model.addAttribute("currentUser", currentUser);
        model.addAttribute("users", allMembers);
        model.addAttribute("lastMessages", lastMessages);

        return "frontend/chat/chat-mvp";
    }
}
