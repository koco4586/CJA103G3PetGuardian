package com.petguardian.chat.service.chatroom;

import com.petguardian.chat.dto.ChatRoomDTO;
import com.petguardian.chat.dto.ChatRoomMetadataDTO;
import com.petguardian.chat.dto.MemberProfileDTO;
import com.petguardian.chat.model.ChatRoomEntity;
import com.petguardian.chat.service.mapper.ChatRoomMapper;
import com.petguardian.chat.service.context.ChatPageContext;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Domain Facade for Chat Room Operations.
 * 
 * Responsibilities:
 * - Unified Entry Point for all Chatroom and Member Profile data operations
 * - Resilience Aspect Boundary ({@code @CircuitBreaker})
 * - Coordinates Internal Workers (DataManager, Creator, Verifier)
 * 
 * Design Principles:
 * - Pure Encapsulation: Controllers and ChatServiceImpl do not know about
 * Redis/MySQL implementation details
 * - Thin Facade: Delegates implementation to package-private managers
 * - Batch Optimization: Uses MGET operations where possible to reduce N+1 query
 * patterns
 * 
 * @see ChatRoomMetadataService Internal data manager (package-private)
 * @see ChatRoomCreationStrategy Strategy for idling/new room resolution
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ChatRoomService {

    private final ChatRoomMetadataService dataManager;
    private final ChatRoomCreationStrategy roomCreator;
    private final ChatRoomMapper mapper;

    // =========================================================================
    // READ OPERATIONS - Member Profiles
    // =========================================================================

    /**
     * Retrieves a single member profile.
     * Uses cache-aside pattern: Redis first, fallback to MySQL.
     *
     * @param memberId Member ID
     * @return MemberProfileDTO or null if not found
     */
    @CircuitBreaker(name = "chatroomRead", fallbackMethod = "fallbackGetMemberProfile")
    public MemberProfileDTO getMemberProfile(Integer memberId) {
        return dataManager.getMemberProfile(memberId);
    }

    /**
     * Fallback Strategy for Profile Retrieval.
     * State: Graceful Degradation (Triggered when Redis/DB is saturated or circuit
     * is OPEN).
     */
    protected MemberProfileDTO fallbackGetMemberProfile(Integer memberId, Throwable t) {
        log.warn("[ChatRoomService] getMemberProfile fallback for memberId={}: {}",
                memberId, t.getMessage());
        return null;
    }

    /**
     * Retrieves multiple member profiles in batch.
     * Optimized for batch loading to avoid N+1 queries.
     *
     * @param memberIds List of member IDs
     * @return Map of memberId to MemberProfileDTO
     */
    @CircuitBreaker(name = "chatroomRead", fallbackMethod = "fallbackGetMemberProfiles")
    public Map<Integer, MemberProfileDTO> getMemberProfiles(List<Integer> memberIds) {
        if (memberIds == null || memberIds.isEmpty()) {
            return Collections.emptyMap();
        }
        return dataManager.getMemberProfiles(memberIds);
    }

    /**
     * Fallback Strategy for Batch Profile Retrieval.
     * Ensures UI stability by returning empty context instead of failing the
     * request.
     */
    protected Map<Integer, MemberProfileDTO> fallbackGetMemberProfiles(List<Integer> memberIds, Throwable t) {
        log.warn("[ChatRoomService] getMemberProfiles fallback: {}", t.getMessage());
        return Collections.emptyMap();
    }

    // =========================================================================
    // READ OPERATIONS - Chatroom Metadata
    // =========================================================================

    /**
     * Retrieves chatroom metadata by ID.
     *
     * @param chatroomId Chatroom ID
     * @return ChatRoomMetadataDTO or null if not found
     */
    @CircuitBreaker(name = "chatroomRead", fallbackMethod = "fallbackGetRoomMetadata")
    public ChatRoomMetadataDTO getRoomMetadata(Integer chatroomId) {
        return dataManager.getRoomMetadata(chatroomId);
    }

    /**
     * Fallback Strategy for Room Metadata.
     * Maintains system availability under high load or network partitions.
     */
    protected ChatRoomMetadataDTO fallbackGetRoomMetadata(Integer chatroomId, Throwable t) {
        log.warn("[ChatRoomService] getRoomMetadata fallback for chatroomId={}: {}",
                chatroomId, t.getMessage());
        return null;
    }

    /**
     * Retrieves all chatrooms for a user with partner profile synchronization.
     * 
     * Workflow:
     * 1. Snapshot: Fetch base chatroom identities for target user
     * 2. Batch Projection: Resolve all partner profiles in a single I/O pass
     * 3. Transformation: Map to display-optimized DTOs sorted by message recency
     *
     * @param userId User ID
     * @return List of {@link ChatRoomDTO} sorted by lastMessageTime descending
     */
    @CircuitBreaker(name = "chatroomRead", fallbackMethod = "fallbackGetUserChatrooms")
    public List<ChatRoomDTO> getUserChatrooms(Integer userId) {
        // 1. Fetch chatroom metadata
        List<ChatRoomMetadataDTO> metadataList = dataManager.getUserChatrooms(userId);
        if (metadataList == null || metadataList.isEmpty()) {
            return Collections.emptyList();
        }

        // 2. Batch load partner profiles
        Set<Integer> partnerIds = metadataList.stream()
                .flatMap(m -> m.getMemberIds().stream())
                .filter(id -> !id.equals(userId))
                .collect(Collectors.toSet());

        Map<Integer, MemberProfileDTO> memberMap = dataManager
                .getMemberProfiles(new java.util.ArrayList<>(partnerIds));

        // 3. Map to DTOs and sort by latest activity
        return metadataList.stream()
                .map(meta -> mapper.toDtoFromMeta(meta, userId, memberMap))
                .sorted(Comparator.comparing(
                        ChatRoomDTO::getLastMessageTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());
    }

    /**
     * Fallback Strategy for User Chatroom List.
     * Prevents cascading failures in sidebar rendering.
     */
    protected List<ChatRoomDTO> fallbackGetUserChatrooms(Integer userId, Throwable t) {
        log.error("[ChatRoomService] getUserChatrooms fallback for userId={}: {}",
                userId, t.getMessage());
        return Collections.emptyList();
    }

    // =========================================================================
    // OPTIMIZED READ OPERATIONS (Facade)
    // =========================================================================

    /**
     * Batch Context Projection for the Chat Main Page.
     * Resolves Current User Profile, Room List, and Partner Identities in a
     * single coherent context. Eliminates N+1 query patterns.
     * 
     * @param userId Current User ID
     * @return {@link ChatPageContext} containing all page-level dependencies
     */
    @CircuitBreaker(name = "chatroomRead", fallbackMethod = "fallbackGetUserChatroomsWithContext")
    public ChatPageContext getUserChatroomsWithCurrentUser(Integer userId) {
        // 1. Fetch chatroom metadata (List)
        List<ChatRoomMetadataDTO> metadataList = dataManager.getUserChatrooms(userId);
        if (metadataList == null)
            metadataList = Collections.emptyList();

        // 2. Collect ALL involved Member IDs (Current User + All Partners)
        Set<Integer> allMemberIds = new java.util.HashSet<>();
        allMemberIds.add(userId); // Always include current user

        metadataList.forEach(m -> allMemberIds.addAll(m.getMemberIds()));

        // 3. Batch Fetch All Profiles (Single Cache/DB Access)
        Map<Integer, MemberProfileDTO> profileMap = dataManager
                .getMemberProfiles(new java.util.ArrayList<>(allMemberIds));

        // 4. Resolve Current User Profile
        MemberProfileDTO currentUser = profileMap.get(userId);
        if (currentUser == null) {
            // Fallback for current user if missing
            currentUser = MemberProfileDTO.builder()
                    .memberId(userId)
                    .memberName("User " + userId)
                    .build();
        }

        // 5. Construct ChatRoomDTOs using the pre-fetched map
        List<ChatRoomDTO> chatrooms = metadataList.stream()
                .map(meta -> mapper.toDtoFromMeta(meta, userId, profileMap))
                .sorted(Comparator.comparing(
                        ChatRoomDTO::getLastMessageTime,
                        Comparator.nullsLast(Comparator.reverseOrder())))
                .collect(Collectors.toList());

        return ChatPageContext.builder()
                .currentUser(currentUser)
                .chatrooms(chatrooms)
                .memberMap(profileMap)
                .build();
    }

    /**
     * Fallback Strategy for Chat Page Context.
     * Reconstructs a minimal valid state for the UI to prevent total page crash.
     */
    protected ChatPageContext fallbackGetUserChatroomsWithContext(Integer userId,
            Throwable t) {
        log.error("[ChatRoomService] Page Context Fallback: {}", t.getMessage());
        return ChatPageContext.builder()
                .currentUser(MemberProfileDTO.builder().memberId(userId).memberName("Fallback").build())
                .chatrooms(Collections.emptyList())
                .build();
    }

    /**
     * Retrieves raw chatroom metadata list for status calculations.
     * 
     * Unlike getUserChatrooms(), this returns metadata without
     * additional DTO transformation.
     *
     * @param userId User ID
     * @return List of ChatRoomMetadataDTO
     */
    public List<ChatRoomMetadataDTO> getUserChatroomMetadata(Integer userId) {
        List<ChatRoomMetadataDTO> result = dataManager.getUserChatrooms(userId);
        return result != null ? result : Collections.emptyList();
    }

    // =========================================================================
    // WRITE OPERATIONS
    // =========================================================================

    /**
     * Idempotent Resolution for Chatrooms.
     * Retrieves an existing room or initializes a new normalized state.
     * Uses normalized ID ordering to prevent cross-account duplicates.
     *
     * @param userA        First user ID
     * @param userB        Second user ID
     * @param chatroomType Room type (0=Service, 1=Product)
     * @return {@link ChatRoomDTO} representing the unique room identity
     */
    public ChatRoomDTO findOrCreateChatroom(Integer userA, Integer userB, Integer chatroomType) {
        ChatRoomEntity entity = roomCreator.findOrCreate(userA, userB, chatroomType);

        // Resolve partner name for display
        Integer partnerId = entity.getOtherMemberId(userA);
        MemberProfileDTO partner = dataManager.getMemberProfile(partnerId);
        String partnerName = (partner != null) ? partner.getMemberName() : "Unknown User";

        return mapper.toDto(entity, userA, partnerName);
    }

    /**
     * Internal State Reconstruction for Message Flow.
     * Hydrates a minimal persistence entity from the current metadata context.
     * Optimized for high-frequency writes during message processing.
     *
     * @param chatroomId Optional existing chatroom ID
     * @param senderId   Sender ID (used if creating new room)
     * @param receiverId Receiver ID (used if creating new room)
     * @return {@link ChatRoomEntity}
     */
    public ChatRoomEntity resolveOrCreateChatroom(Integer chatroomId, Integer senderId, Integer receiverId) {
        if (chatroomId != null) {
            ChatRoomMetadataDTO meta = dataManager.getRoomMetadata(chatroomId);
            if (meta != null) {
                // Convert metadata back to minimal entity for persistence layer
                ChatRoomEntity entity = new ChatRoomEntity();
                entity.setChatroomId(meta.getChatroomId());
                entity.setMemId1(meta.getMemberIds().get(0));
                entity.setMemId2(meta.getMemberIds().get(1));
                entity.setChatroomType(meta.getChatroomType());
                entity.setChatroomStatus(meta.getChatroomStatus());
                return entity;
            }
        }
        // Fallback: Create new chatroom with default type 0
        return roomCreator.findOrCreate(senderId, receiverId, 0);
    }

    /**
     * Synchronizes room metadata after a message is sent.
     * Updates lastMessagePreview and lastMessageAt in both DB and cache.
     *
     * @param chatroomId Chatroom ID
     * @param preview    Message preview text
     * @param time       Message timestamp
     * @param senderId   Sender ID
     */
    public void syncRoomMetadata(Integer chatroomId, String preview,
            LocalDateTime time, Integer senderId) {
        dataManager.syncRoomMetadata(chatroomId, preview, time, senderId);
    }

    /**
     * Updates the last read timestamp for a user in a chatroom.
     *
     * @param chatroomId Chatroom ID
     * @param userId     User ID
     * @param time       Read timestamp
     */
    public void updateLastReadAt(Integer chatroomId, Integer userId, LocalDateTime time) {
        dataManager.updateLastReadAt(chatroomId, userId, time);
    }

    // =========================================================================
    // ACCESS CONTROL
    // =========================================================================

    /**
     * Access Control Guard for Room Entry.
     * Enforces membership invariants and throws exceptions upon violation.
     * Strategy: Fail-Fast (Prevents unauthorized processing of downstream logic).
     *
     * @param chatroomId Chatroom ID
     * @param userId     User ID to verify
     * @return {@link ChatRoomMetadataDTO} if verification succeeds
     * @throws IllegalArgumentException if chatroom not found
     * @throws SecurityException        if user is not a member
     */
    public ChatRoomMetadataDTO verifyMembership(Integer chatroomId, Integer userId) {
        ChatRoomMetadataDTO meta = dataManager.getRoomMetadata(chatroomId);
        if (meta == null) {
            throw new IllegalArgumentException("Chatroom not found: " + chatroomId);
        }

        if (!meta.getMemberIds().contains(userId)) {
            throw new SecurityException(
                    "Access denied: User " + userId + " is not a member of chatroom " + chatroomId);
        }

        return meta;
    }

    /**
     * Checks if a user is a member of a chatroom without throwing exceptions.
     *
     * @param chatroomId Chatroom ID
     * @param userId     User ID
     * @return true if user is a member, false otherwise
     */
    public boolean isMember(Integer chatroomId, Integer userId) {
        ChatRoomMetadataDTO meta = dataManager.getRoomMetadata(chatroomId);
        if (meta == null) {
            return false;
        }
        return meta.getMemberIds().contains(userId);
    }
}
