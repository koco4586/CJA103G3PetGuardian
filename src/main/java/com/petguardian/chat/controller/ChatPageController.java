package com.petguardian.chat.controller;

import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.petguardian.chat.dto.ChatRoomDTO;
import com.petguardian.chat.dto.MemberProfileDTO;
import com.petguardian.chat.service.context.ChatPageContext;
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

    // =========================================================================
    // CONSTANTS
    // =========================================================================

    private static final int TYPE_SITTER = 0;
    private static final int TYPE_STORE = 1;
    private static final int TYPE_SENTINEL = -1;

    private static final String SOURCE_STORE = "store";
    private static final String SOURCE_BOOKING = "booking";
    private static final String SOURCE_SITTER = "sitter";

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
        ChatPageContext pageContext = chatRoomService.getUserChatroomsWithCurrentUser(userId);

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
     * Handles connection requests from external modules (Store, Booking, Sitter).
     * Creates/Finds a chatroom and redirects to the chat page with the room active.
     */
    @PostMapping("/chat/connect/{source}")
    public String connectToChat(
            @PathVariable String source,
            @RequestParam Integer targetUserId,
            @RequestParam(defaultValue = "-1") Integer chatroomType,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        Integer currentUserId = authStrategyService.getCurrentUserId(request);

        // Resolve Fallback URL & Default Type (Case Insensitive)
        String fallbackUrl = "/";
        int defaultType = TYPE_SITTER;

        if (SOURCE_STORE.equalsIgnoreCase(source)) {
            fallbackUrl = "/store";
            defaultType = TYPE_STORE;
        } else if (SOURCE_BOOKING.equalsIgnoreCase(source)) {
            fallbackUrl = "/booking/memberOrders";
            defaultType = TYPE_SITTER;
        } else if (SOURCE_SITTER.equalsIgnoreCase(source)) {
            fallbackUrl = "/sitter/bookings";
            defaultType = TYPE_SITTER;
        }

        if (currentUserId == null)
            return "redirect:" + fallbackUrl;

        // Prevent Self-Chat
        if (currentUserId.equals(targetUserId)) {
            return "redirect:" + fallbackUrl;
        }

        // Use resolved default if parameter was not provided (Sentinel check)
        int finalType = (chatroomType == TYPE_SENTINEL) ? defaultType : chatroomType;

        // Create or find chatroom (via unified facade)
        ChatRoomDTO room = chatRoomService.findOrCreateChatroom(
                currentUserId, targetUserId, finalType);

        redirectAttributes.addFlashAttribute("activeRoomId", room.getChatroomId());
        return "redirect:/chat";
    }
}
