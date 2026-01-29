package com.petguardian.chat.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.petguardian.chat.dto.ChatMemberDTO;
import com.petguardian.chat.dto.ChatRoomDTO;
import com.petguardian.common.service.AuthStrategyService;
import com.petguardian.chat.service.chatmessage.ChatPageService;
import com.petguardian.chat.service.ChatService;

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
    private final ChatService chatService;

    public ChatPageController(ChatPageService chatPageService,
            AuthStrategyService authStrategyService,
            ChatService chatService) {
        this.chatPageService = chatPageService;
        this.authStrategyService = authStrategyService;
        this.chatService = chatService;
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

        // Security Check
        if (userId == null) {
            model.addAttribute("error", "請先登入");
            return "redirect:/front/loginpage";
        }

        // Context Preparation

        // 1. Current User (Metadata Cache First)
        ChatMemberDTO currentUser = chatPageService.getMember(userId);
        if (currentUser == null) {
            // Fallback for extreme cache misses - stay minimal to avoid DB if possible
            currentUser = new ChatMemberDTO(userId, "User " + userId);
        }

        // 2. Chatroom List (Sidebar)
        List<ChatRoomDTO> chatrooms = chatPageService.getMyChatrooms(userId);

        // View Model Population
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("chatrooms", chatrooms);

        // [NEW] Inject activeRoomId if present in FlashMap/Model (from
        // RedirectAttributes)
        if (model.containsAttribute("activeRoomId")) {
            model.addAttribute("activeRoomId", model.getAttribute("activeRoomId"));
        }

        // Note: Sidebar data consolidated in ChatRoomDTO (via getMyChatrooms)

        return "frontend/chat/chat-mvp";
    }

    // ============================================================
    // ACTION ENDPOINTS
    // ============================================================

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

        // Basic Security: Prevent Self-Chat
        if (currentUserId.equals(targetUserId)) {
            return "redirect:/store";
        }

        // Reuse existing creation/find logic (Service delegated)
        ChatRoomDTO room = chatService.findOrCreateChatroom(currentUserId, targetUserId,
                chatroomType);

        redirectAttributes.addFlashAttribute("activeRoomId", room.getChatroomId());
        return "redirect:/chat";
    }
}
