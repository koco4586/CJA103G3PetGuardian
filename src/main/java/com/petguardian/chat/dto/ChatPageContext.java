package com.petguardian.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Optimized Context for rendering the main Chat Page (MVP).
 * Batches "Current User Profile" and "Chat Room List" (with their members)
 * to avoid N+1 Selects.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatPageContext implements Serializable {

    // The profile of the user viewing the page
    private MemberProfileDTO currentUser;

    // The list of rooms
    private List<ChatRoomDTO> chatrooms;

    // Optional: Map of all involved member profiles for quick lookup
    // (Optimization for future features like "Seen By")
    private Map<Integer, MemberProfileDTO> memberMap;
}
