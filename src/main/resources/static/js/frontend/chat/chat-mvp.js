/**
 * Chat MVP Module
 * Handles WebSocket integration, state management, and UI rendering.
 * 
 * Architecture:
 * - IIFE pattern for namespace isolation
 * - Centralized configuration and DOM references
 * - async/await for cleaner async flow
 */

// ============================================================
// CONFIGURATION
// ============================================================
const CONFIG = {
    PAGE_SIZE: 50,
    RECONNECT_DELAY_MS: 5000,
    RECONNECT_JITTER_MS: 2000, // Random jitter to prevent thundering herd
    SCROLL_THRESHOLD_PX: 100,
    DEBOUNCE_DELAY_MS: 300
};

// ============================================================
// STATE CONTAINER
// ============================================================
const ChatState = {
    stompClient: null,
    currentUserId: null,
    currentUserName: null,
    selectedUserId: null,
    selectedUserName: null,
    currentReplyId: null,
    loadingRequestId: 0,
    currentPage: 0,
    isLoadingHistory: false,
    hasMoreHistory: true,
    isLoadingHistory: false,
    hasMoreHistory: true,
    messageIds: new Set(), // O(1) deduplication
    currentChatroomId: null,
    // Partner context for sending messages
    currentPartnerId: null,
    // Read receipt subscription
    readSubscription: null
};

// ============================================================
// DOM CACHE (Initialized once on DOMContentLoaded)
// ============================================================
const DOM = {
    messageList: null,
    msgInput: null,
    sendBtn: null,
    chatHeaderTitle: null,
    chatInputForm: null,
    replyPreviewBar: null,
    replyToName: null,
    replyToText: null,

    init() {
        this.messageList = document.getElementById('message-list');
        this.msgInput = document.getElementById('msg-input');
        this.sendBtn = document.getElementById('send-btn');
        this.chatHeaderTitle = document.getElementById('chatHeaderTitle');
        this.chatInputForm = document.getElementById('chat-input-form');
        this.replyPreviewBar = document.getElementById('reply-preview-bar');
        this.replyToName = document.getElementById('reply-to-name');
        this.replyToText = document.getElementById('reply-to-text');

        // Event Listeners
        if (this.msgInput) {
            this.msgInput.addEventListener('input', updateSendButton);
        }
    }
};

// ============================================================
// INITIALIZATION
// ============================================================
function initChat() {
    const userIdEl = document.getElementById('currentUserId');
    const userNameEl = document.getElementById('currentUserName');

    // Guard: Required elements must exist
    if (!userIdEl || !userNameEl) {
        console.error('[Chat] Missing required hidden inputs: currentUserId or currentUserName');
        return;
    }

    ChatState.currentUserId = parseInt(userIdEl.value, 10);
    ChatState.currentUserName = userNameEl.value;

    DOM.init();
    updateSendButton(); // Initialize button state
    connect();

    // [NEW] Auto-select room if requested (e.g. redirected from Store)
    const initialRoomId = document.getElementById('initialChatroomId');
    if (initialRoomId && initialRoomId.value) {
        const roomId = initialRoomId.value;
        // Find partner details from the DOM sidebar item
        const sidebarItem = document.getElementById('contact-item-' + roomId);
        if (sidebarItem) {
            ChatApp.selectRoom(
                roomId,
                sidebarItem.dataset.partnerName,
                sidebarItem.dataset.partnerId
            );
        } else {
            console.warn('[Chat] Requested room ID ' + roomId + ' not found in sidebar list.');
            // Fallback: Could fetch room details via API if needed, 
            // but controller should have assured it's in the list.
        }
    }
}

// ============================================================
// WEBSOCKET CONNECTION
// ============================================================
function connect() {
    const socket = new SockJS('/ws');
    ChatState.stompClient = Stomp.over(socket);

    // Disable STOMP debug logging in production
    ChatState.stompClient.debug = null;

    ChatState.stompClient.connect({}, function (frame) {
        // Subscribe to personal message topic
        ChatState.stompClient.subscribe(
            '/topic/messages.' + ChatState.currentUserId,
            function (message) {
                onMessageReceived(JSON.parse(message.body));
            }
        );

        // [NEW] If we entered a room while connecting, subscribe to it now
        if (ChatState.currentChatroomId) {
            subscribeToReadReceipts(ChatState.currentChatroomId);
        }
    }, function (error) {
        console.error('[WS] Connection error:', error);
        // Jittered reconnect to prevent thundering herd when server restarts
        const jitter = Math.floor(Math.random() * CONFIG.RECONNECT_JITTER_MS);
        setTimeout(connect, CONFIG.RECONNECT_DELAY_MS + jitter);
    });
}

// ============================================================
// MESSAGE HANDLERS
// ============================================================

/**
 * Process incoming WebSocket message.
 * Determines if message should be appended to current view or trigger notification.
 */
function onMessageReceived(dto) {
    const senderId = parseInt(dto.senderId, 10);
    const receiverId = parseInt(dto.receiverId, 10);
    const isSentByMe = (senderId === ChatState.currentUserId);
    const partnerId = isSentByMe ? receiverId : senderId;

    // Determine if message belongs to currently open conversation by Chatroom ID
    let shouldAppend = false;
    let activeChatroomId = null;

    if (ChatState.currentChatroomId && dto.chatroomId) {
        if (dto.chatroomId === ChatState.currentChatroomId) {
            shouldAppend = true;
            activeChatroomId = ChatState.currentChatroomId;
        }
    } else if (ChatState.selectedUserId) {
        // Fallback for legacy messages without chatroomId (should not happen with new backend)
        shouldAppend = isSentByMe
            ? (receiverId === ChatState.selectedUserId)
            : (senderId === ChatState.selectedUserId);
    }

    if (shouldAppend) {
        appendMessage(dto.content, isSentByMe, dto.senderName, dto.messageId, dto.replyToContent, dto.replyToSenderName);

        // Real-time "Mark as Read"
        if (!isSentByMe && activeChatroomId) {
            // Check if window is focused? For now assume yes if connected.
            // Ideally check document.hidden but "Best Effort" MVP is fine.
            markAsRead(activeChatroomId);
        }

    } else if (!isSentByMe) {
        // Show unread indicator for specific room
        if (dto.chatroomId) {
            showUnreadDotForRoom(dto.chatroomId);
        } else {
            // Fallback
            showUnreadDot(partnerId);
        }
    }

    // Always update sidebar preview
    let previewContent = dto.content;
    if (isSentByMe) {
        previewContent = 'You: ' + previewContent;
    }

    // Truncate if too long (consistent with backend logic)
    if (previewContent.length > 30) {
        previewContent = previewContent.substring(0, 27) + '...';
    }

    updateContactLastMessage(dto.chatroomId, partnerId, previewContent);
}

function showUnreadDotForRoom(chatroomId) {
    const contactItem = document.getElementById('contact-item-' + chatroomId);
    if (contactItem) {
        const dot = contactItem.querySelector('.unread-dot');
        if (dot) dot.classList.remove('app-d-none');
    }
}

function showUnreadDot(userId) {
    // Deprecated but kept for fallback
    // Try to find ANY room with this user? 
    // Ideally we shouldn't use this anymore.
}

function updateContactLastMessage(chatroomId, userId, content) {
    // ID-based selection uses chatroomId now
    let contactItem;
    if (chatroomId) {
        contactItem = document.getElementById('contact-item-' + chatroomId);
    }

    // Fallback if chatroomId missing (shouldn't happen)
    // if (!contactItem) contactItem = document.getElementById('contact-item-' + userId);

    if (contactItem) {
        const msgDiv = contactItem.querySelector('.contact-last-msg');
        if (msgDiv) msgDiv.textContent = content;

        // Move to top (Recent activity sort)
        // const list = contactItem.parentNode;
        // list.prepend(contactItem);
    }
}

// ============================================================
// USER SELECTION
// ============================================================
function selectRoom(chatroomId, partnerName, partnerId) {
    ChatState.currentChatroomId = parseInt(chatroomId, 10);
    ChatState.selectedUserId = parseInt(partnerId, 10); // Check if HTML passes this
    ChatState.selectedUserName = partnerName;

    // Update sidebar active state
    document.querySelectorAll('.contact-item').forEach(item => {
        item.classList.remove('active');
        if (parseInt(item.dataset.chatroomId, 10) === ChatState.currentChatroomId) {
            item.classList.add('active');
            // Clear unread indicator
            const dot = item.querySelector('.unread-dot');
            if (dot) dot.classList.add('app-d-none');
        }
    });

    // Update chat header
    if (DOM.chatHeaderTitle) {
        DOM.chatHeaderTitle.textContent = partnerName;
    }

    // Show loading state
    if (DOM.messageList) {
        DOM.messageList.innerHTML = '<div class="chat-empty-state"><i class="fas fa-spinner fa-spin"></i><p>Loading...</p></div>';
    }

    if (DOM.chatInputForm) {
        DOM.chatInputForm.style.display = 'flex';
    }

    // Subscribe to read receipts for this room
    if (ChatState.stompClient && ChatState.stompClient.connected) {
        subscribeToReadReceipts(ChatState.currentChatroomId);
    }

    loadChatHistory(ChatState.currentChatroomId);
}

/**
 * Safely manages read receipt subscription
 */
function subscribeToReadReceipts(chatroomId) {
    if (ChatState.readSubscription) {
        ChatState.readSubscription.unsubscribe();
        ChatState.readSubscription = null;
    }

    // Double check connection before subscribing
    if (ChatState.stompClient && ChatState.stompClient.connected) {
        ChatState.readSubscription = ChatState.stompClient.subscribe(
            '/topic/chatroom.' + chatroomId + '.read',
            function () {
                markLastMessageAsRead();
            }
        );
    }
}

/**
 * Shows 已讀 on the last sent message only.
 * Removes any existing read status first.
 */
function markLastMessageAsRead() {
    // Remove all existing read status
    document.querySelectorAll('.read-status').forEach(el => el.remove());

    const messages = document.querySelectorAll('.message.sent');
    if (messages.length === 0) return;
    const lastSent = messages[messages.length - 1];
    if (lastSent) {
        const span = document.createElement('span');
        span.className = 'read-status';
        span.textContent = '已讀';
        lastSent.appendChild(span);
    }
}

// ============================================================
// CHAT HISTORY (async/await for clarity)
// ============================================================

/**
 * Marks the current room as read.
 */
/**
 * Marks the current room as read.
 */
function markAsRead(chatroomId) {
    if (!chatroomId) return;

    fetch(`/api/chatrooms/${chatroomId}/read?userId=${ChatState.currentUserId}`, { method: 'POST' })
        .then(res => res.json())
        .then(data => {
            // Update global header dot based on server response (remaining unread count)
            const dotEl = document.getElementById('header-chat-dot');
            if (dotEl) {
                dotEl.style.display = data.hasUnread ? 'block' : 'none';
            }
        })
        .catch(console.error);
}

/**
 * Load chat history for selected user.
 * Uses requestId pattern to handle race conditions when user rapidly switches conversations.
 * 
 * Flow:
 * 1. Resolve chatroom by partner ID
 * 2. Fetch paginated messages
 * 3. Render to DOM
 */
async function loadChatHistory(chatroomId, isLoadMore = false) {
    // Reset pagination on new conversation
    if (!isLoadMore) {
        ChatState.currentPage = 0;
        ChatState.hasMoreHistory = true;
        ChatState.loadingRequestId++; // Invalidate any in-flight requests
        ChatState.messageIds.clear(); // Clear cache on new chat
    }

    const requestId = ChatState.loadingRequestId;
    ChatState.isLoadingHistory = true;
    const page = ChatState.currentPage;

    try {
        // Step 1: Mark as Read Immediately
        // Optimization: Don't wait for history to mark as read
        markAsRead(chatroomId);

        // Step 2: Fetch messages for specific room
        const messagesResponse = await fetch(
            `/api/chatrooms/${chatroomId}/messages?userId=${ChatState.currentUserId}&page=${page}&size=${CONFIG.PAGE_SIZE}`
        );

        if (requestId !== ChatState.loadingRequestId) return;

        if (!messagesResponse.ok) {
            if (messagesResponse.status === 403) {
                showEmptyState('Access Denied');
            } else {
                showEmptyState(`Start chatting with ${ChatState.selectedUserName}`);
            }
            ChatState.isLoadingHistory = false;
            return;
        }

        const messages = await messagesResponse.json();
        ChatState.isLoadingHistory = false;

        // Check if more history exists
        if (messages.length < CONFIG.PAGE_SIZE) {
            ChatState.hasMoreHistory = false;
        }

        // Step 3: Render
        if (!isLoadMore) {
            DOM.messageList.innerHTML = '';
            if (messages.length === 0) {
                showEmptyState(`Start chatting with ${ChatState.selectedUserName}`);
            } else {
                renderMessagesBatch(messages, DOM.messageList, true);
                attachScrollListener(DOM.messageList, chatroomId); // Attach to room ID

                // Check if partner has read (any message has isRead=true)
                if (messages.some(m => m.isRead)) {
                    markLastMessageAsRead();
                }
            }
        } else if (messages.length > 0) {
            renderMessagesBatch(messages, DOM.messageList, false);
        }

    } catch (error) {
        if (requestId !== ChatState.loadingRequestId) return;
        console.error('[History] Error:', error);
        ChatState.isLoadingHistory = false;
        if (!isLoadMore) {
            showEmptyState('Load failed', 'fa-exclamation-triangle');
        }
    }
}

function showEmptyState(message, icon = 'fa-comments') {
    if (DOM.messageList) {
        DOM.messageList.innerHTML = `<div class="chat-empty-state"><i class="fas ${icon}"></i><p>${message}</p></div>`;
    }
}

// ============================================================
// MESSAGE RENDERING
// ============================================================

/**
 * Batch render messages using DocumentFragment for performance.
 * @param {Array} messages - Message DTOs
 * @param {HTMLElement} container - Target container
 * @param {boolean} scrollToBottom - true for initial load, false for history prepend
 */
function renderMessagesBatch(messages, container, scrollToBottom) {
    const fragment = document.createDocumentFragment();

    messages.forEach(msg => {
        // Skip duplicates (O(1) check)
        if (msg.messageId && ChatState.messageIds.has(msg.messageId)) {
            return;
        }

        // Add to cache
        if (msg.messageId) ChatState.messageIds.add(msg.messageId);

        const isSentByMe = (msg.senderId === ChatState.currentUserId);
        const msgEl = createMessageElement(msg.content, isSentByMe, msg.senderName, msg.messageId, msg.replyToContent, msg.replyToSenderName, msg.isRead);
        fragment.appendChild(msgEl);
    });

    if (scrollToBottom) {
        container.appendChild(fragment);
        container.scrollTop = container.scrollHeight;
    } else {
        // Prepend: maintain scroll position relative to existing content
        const oldScrollHeight = container.scrollHeight;
        container.insertBefore(fragment, container.firstChild);
        const newScrollHeight = container.scrollHeight;
        container.scrollTop = newScrollHeight - oldScrollHeight;
    }
}

/**
 * Create message DOM element (XSS-safe via createTextNode).
 */
function createMessageElement(content, isSent, senderName, messageId, replyToContent, replyToSenderName, isRead) {
    const msgDiv = document.createElement('div');
    msgDiv.className = 'message ' + (isSent ? 'sent' : 'received');

    if (messageId) {
        msgDiv.dataset.messageId = messageId;
        msgDiv.style.cursor = 'pointer';
        msgDiv.addEventListener('click', function () {
            // Ignore if user is selecting text
            if (window.getSelection().toString().length > 0) return;
            toggleReply(msgDiv, messageId, content, senderName);
        });
    }

    // Reply context (if this message is a reply)
    if (replyToContent) {
        const contextDiv = document.createElement('div');
        contextDiv.className = 'reply-context';

        const nameStrong = document.createElement('strong');
        nameStrong.textContent = replyToSenderName + ': ';
        contextDiv.appendChild(nameStrong);

        const contentSpan = document.createElement('span');
        contentSpan.textContent = replyToContent;
        contextDiv.appendChild(contentSpan);

        msgDiv.appendChild(contextDiv);
    }

    // Message body (XSS-safe)
    msgDiv.appendChild(document.createTextNode(content));

    return msgDiv;
}

/**
 * Append single message and smart-scroll.
 */
function appendMessage(content, isSent, senderName, messageId, replyToContent, replyToSenderName) {
    // Duplicate guard (O(1))
    if (messageId && ChatState.messageIds.has(messageId)) {
        return;
    }

    if (messageId) ChatState.messageIds.add(messageId);

    // Clear empty state if present
    const emptyState = DOM.messageList.querySelector('.chat-empty-state');
    if (emptyState) {
        emptyState.remove();
    }

    const msgDiv = createMessageElement(content, isSent, senderName, messageId, replyToContent, replyToSenderName);

    // Smart scroll: only auto-scroll if user is near bottom or just sent a message
    const isNearBottom = (DOM.messageList.scrollHeight - DOM.messageList.scrollTop - DOM.messageList.clientHeight) < CONFIG.SCROLL_THRESHOLD_PX;
    DOM.messageList.appendChild(msgDiv);

    if (isSent || isNearBottom) {
        DOM.messageList.scrollTop = DOM.messageList.scrollHeight;
    }
}

// ============================================================
// INFINITE SCROLL
// ============================================================
function attachScrollListener(element, partnerId) {
    element.onscroll = function () {
        // Load more when scrolled to top
        if (element.scrollTop === 0 && ChatState.hasMoreHistory && !ChatState.isLoadingHistory) {
            ChatState.currentPage++;
            loadChatHistory(partnerId, true);
        }
    };
}

// ============================================================
// MESSAGE SENDING
// ============================================================
function sendMessage() {
    const content = DOM.msgInput.value.trim();

    if (!content) return;
    if (!ChatState.selectedUserId) {
        alert('Select a user first');
        return;
    }

    const dto = {
        senderId: ChatState.currentUserId,
        receiverId: ChatState.selectedUserId,
        content: content,
        senderName: ChatState.currentUserName,
        replyToId: ChatState.currentReplyId,
        chatroomId: ChatState.currentChatroomId
    };

    ChatState.stompClient.send('/app/chat.send', {}, JSON.stringify(dto));

    // Optimistic UI: update sidebar immediately
    updateContactLastMessage(ChatState.currentChatroomId, ChatState.selectedUserId, 'You: ' + content);

    cancelReply();
    DOM.msgInput.value = '';
    updateSendButton(); // Explicitly update button state after clear
    DOM.msgInput.focus();
}

// ============================================================
// REPLY SYSTEM
// ============================================================
function toggleReply(msgElement, messageId, content, senderName) {
    // Toggle off if clicking same message
    if (ChatState.currentReplyId === messageId) {
        cancelReply();
        return;
    }

    // Clear previous selection
    document.querySelectorAll('.message.reply-selected').forEach(el => el.classList.remove('reply-selected'));

    ChatState.currentReplyId = messageId;
    msgElement.classList.add('reply-selected');

    DOM.replyToName.textContent = 'Reply to ' + senderName;
    DOM.replyToText.textContent = content;
    DOM.replyPreviewBar.style.display = 'flex';

    DOM.msgInput.focus();
}

function cancelReply() {
    ChatState.currentReplyId = null;
    document.querySelectorAll('.message.reply-selected').forEach(el => el.classList.remove('reply-selected'));
    DOM.replyPreviewBar.style.display = 'none';
}

// ============================================================
// INPUT HANDLERS
// ============================================================
function handleKeyPress(event) {
    if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault();
        sendMessage();
    }
}

function updateSendButton() {
    if (DOM.msgInput.value.trim() !== '') {
        DOM.sendBtn.classList.add('active');
    } else {
        DOM.sendBtn.classList.remove('active');
    }
}

// ============================================================
// CONTACT SEARCH
// ============================================================
function filterContacts(query) {
    const contactItems = document.querySelectorAll('.contact-item');
    const lowerQuery = query.toLowerCase();

    contactItems.forEach(item => {
        const userName = item.getAttribute('data-partner-name').toLowerCase();
        item.style.display = userName.includes(lowerQuery) ? '' : 'none';
    });
}

// ============================================================
// UTILITIES
// ============================================================
function debounce(func, wait) {
    let timeout;
    return function (...args) {
        clearTimeout(timeout);
        timeout = setTimeout(() => func(...args), wait);
    };
}

// ============================================================
// EXPORTS & INITIALIZATION
// ============================================================
const debouncedFilterContacts = debounce(filterContacts, CONFIG.DEBOUNCE_DELAY_MS);

// Namespace: Avoid global scope pollution, all public APIs under ChatApp
window.ChatApp = {
    handleKeyPress,
    filterContacts: debouncedFilterContacts,
    selectRoom,
    sendMessage,
    cancelReply,
    updateSendButton
};

document.addEventListener('DOMContentLoaded', initChat);
