package com.petguardian.chat.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.petguardian.chat.dto.ChatRoomDTO;
import com.petguardian.chat.dto.MemberProfileDTO;
import com.petguardian.chat.service.chatroom.ChatRoomService;
import com.petguardian.common.service.AuthStrategyService;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

/**
 * MVC Controller for Chat View Rendering.
 * 
 * Responsibilities:
 * Serves the initial HTML page (SSR with Thymeleaf)
 * Pre-loads essential context (Current User, Contact List, Message Previews)
 * Delegates authentication checks to strategy
 * 
 * Uses {@link ChatRoomService} as the unified facade for all chatroom
 * operations.
 */
@Controller
@RequiredArgsConstructor
public class ChatPageController {

    // =========================================================================
    // DEPENDENCIES (Simplified via Facade)
    // =========================================================================

    private final ChatRoomService chatRoomService;
    private final AuthStrategyService authStrategyService;

    // =========================================================================
    // VIEW ENDPOINTS
    // =========================================================================

    /**
     * Entry Point: Chat MVP Interface.
     * 
     * Endpoint: /chat
     * Methods: GET, POST
     * 
     * Orchestrates the initial state required for the chat application:
     * Validates User Session (Redirect/Error if invalid)
     * Loads Current User Profile
     * Fetches Chatroom List for Sidebar
     */
    @RequestMapping(value = "/chat", method = { RequestMethod.GET, RequestMethod.POST })
    public String chatMvpPage(HttpServletRequest request, Model model) {
        Integer userId = authStrategyService.getCurrentUserId(request);

        // Security Check
        if (userId == null) {
            model.addAttribute("error", "請先登入");
            return "redirect:/front/loginpage";
        }

        // 1. Optimized Batch Load (Single Round Trip)
        com.petguardian.chat.dto.ChatPageContext pageContext = chatRoomService.getUserChatroomsWithCurrentUser(userId);

        MemberProfileDTO currentUser = pageContext.getCurrentUser();
        List<ChatRoomDTO> chatrooms = pageContext.getChatrooms();

        // View Model Population
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("chatrooms", chatrooms);

        // Inject activeRoomId if present (from RedirectAttributes)
        if (model.containsAttribute("activeRoomId")) {
            model.addAttribute("activeRoomId", model.getAttribute("activeRoomId"));
        }

        return "frontend/chat/chat-mvp";
    }

    // =========================================================================
    // ACTION ENDPOINTS
    // =========================================================================

    /**
     * Handles connection requests from external modules (Store).
     * Creates/Finds a chatroom and redirects to the chat page with the room active.
     */
    @PostMapping("/chat/connect/store")
    public String connectToStoreChat(
            @RequestParam Integer targetUserId,
            @RequestParam(defaultValue = "1") Integer chatroomType,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        Integer currentUserId = authStrategyService.getCurrentUserId(request);
        if (currentUserId == null) {
            return "redirect:/store";
        }

        // Prevent Self-Chat
        if (currentUserId.equals(targetUserId)) {
            return "redirect:/store";
        }

        // Create or find chatroom (via unified facade)
        ChatRoomDTO room = chatRoomService.findOrCreateChatroom(
                currentUserId, targetUserId, chatroomType);

        redirectAttributes.addFlashAttribute("activeRoomId", room.getChatroomId());
        return "redirect:/chat";
    }
}
