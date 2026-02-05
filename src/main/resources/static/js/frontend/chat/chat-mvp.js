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
    DEBOUNCE_DELAY_MS: 300,
    // [NEW] Date Formatting
    DATE_FORMAT_OPTIONS: { year: 'numeric', month: '2-digit', day: '2-digit' },
    TIME_FORMAT_OPTIONS: { hour: '2-digit', minute: '2-digit', hour12: false }
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
    messageIds: new Set(), // O(1) deduplication
    currentChatroomId: null,
    currentPartnerId: null,
    readSubscription: null,
    contextMenuTargetId: null,
    contextMenuTargetStatus: 0,
    isSearchMode: false,
    isReturningToPresent: false
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
    // [NEW] Reply context for UI
    replyToId: null,      // ID of the message being replied to
    replyToContent: null, // Content preview of the message being replied to
    replyToSender: null,  // Name of the sender being replied to
    // [NEW] Search state
    isSearchMode: false, // Track search state
    // Report Elements
    reportModal: null,
    reportReasonType: null,
    reportReasonText: null,
    contextMenu: null,
    ctxReportBtn: null,

    init() {
        this.messageList = document.getElementById('message-list');
        this.msgInput = document.getElementById('msg-input');
        this.sendBtn = document.getElementById('send-btn');
        this.chatHeaderTitle = document.getElementById('chatHeaderTitle');
        this.chatInputForm = document.getElementById('chat-input-form');
        this.replyPreviewBar = document.getElementById('reply-preview-bar');
        this.replyToName = document.getElementById('reply-to-name');
        this.replyToText = document.getElementById('reply-to-text');

        // Report DOM initialization
        this.reportModal = document.getElementById('reportModal');
        this.reportReasonType = document.getElementById('reportReasonType');
        this.reportReasonText = document.getElementById('reportReasonText');
        this.contextMenu = document.getElementById('contextMenu');
        this.ctxReportBtn = document.getElementById('ctx-report-btn');

        // Global Event Listeners
        if (this.msgInput) {
            this.msgInput.addEventListener('input', updateSendButton);
        }

        // Hide context menu on global click
        document.addEventListener('click', (e) => {
            if (this.contextMenu) this.contextMenu.style.display = 'none';
        });

        // Prevent default context menu on custom menu
        if (this.contextMenu) {
            this.contextMenu.addEventListener('contextmenu', e => e.preventDefault());
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
        appendMessage(dto.content, isSentByMe, dto.senderName, dto.messageId, dto.replyToContent, dto.replyToSenderName, dto.isRead, 0, dto.chatTime);


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
            function (message) {
                const receipt = JSON.parse(message.body);
                // Only act if the OTHER person read it
                if (receipt.readerId !== ChatState.currentUserId) {
                    markAllSentMessagesAsRead();
                }
            }
        );
    }
}

/**
 * Marks all sent messages in the current view as "Read".
 * Does not remove existing ones to prevent "vanishing indicator" effect.
 */
function markAllSentMessagesAsRead() {
    const messages = document.querySelectorAll('.message.sent');
    messages.forEach(msg => {
        // Only add if not already present
        if (!msg.querySelector('.read-status')) {
            const span = document.createElement('span');
            span.className = 'read-status';
            span.textContent = '已讀';
            msg.appendChild(span);
        }
    });
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
 * Enhanced loadChatHistory with direction support
 */
async function loadChatHistory(partnerId, direction = 'INITIAL') {
    if (!partnerId) return;

    const requestId = ++ChatState.loadingRequestId;
    ChatState.isLoadingHistory = true;

    // Clear cache/state for INITIAL load
    if (direction === 'INITIAL') {
        ChatState.currentPage = 0;
        ChatState.hasMoreHistory = true;
        ChatState.messageIds.clear();
        ChatState.isReturningToPresent = false;
        // DO NOT clear innerHTML here yet, preserve the spinner from selectRoom
    } else if (direction === 'OLDER') {
        // Show a small spinner at the top for infinite scroll
        showTopLoadingIndicator();
    }

    // Determine target page (don't commit to ChatState yet)
    let targetPage = ChatState.currentPage;
    if (direction === 'OLDER') targetPage++;
    if (direction === 'NEWER') targetPage--;
    if (targetPage < 0) targetPage = 0;

    try {
        if (direction === 'INITIAL') markAsRead(partnerId);

        const messagesResponse = await fetch(
            `/api/chatrooms/${partnerId}/messages?userId=${ChatState.currentUserId}&page=${targetPage}&size=${CONFIG.PAGE_SIZE}`
        );

        if (requestId !== ChatState.loadingRequestId) return;

        if (!messagesResponse.ok) return;

        const messages = await messagesResponse.json();
        console.log(`[History] Loaded page ${targetPage}, messages: ${messages.length}`);

        // Commit targetPage to ChatState only on success
        ChatState.currentPage = targetPage;

        // Determine Mode
        let renderMode = RenderMode.INITIAL;
        if (direction === 'OLDER') {
            renderMode = RenderMode.PREPEND;
            if (messages.length < CONFIG.PAGE_SIZE) ChatState.hasMoreHistory = false;
        } else if (direction === 'NEWER') {
            renderMode = RenderMode.APPEND;
            ChatState.hasMoreHistory = true; // Still have newer
        }

        if (messages.length > 0 || direction === 'INITIAL') {
            if (messages.length === 0 && direction === 'INITIAL') {
                showEmptyState(`Start chatting!`);
            } else {
                renderMessagesBatch(messages, DOM.messageList, renderMode);
            }

            if (direction === 'INITIAL') attachScrollListener(DOM.messageList, partnerId);
            toggleBackToPresentBtn();

            // Sequential Return Logic
            if (ChatState.isReturningToPresent && ChatState.currentPage > 0) {
                // Short delay for "speed scrolling" effect
                setTimeout(() => loadChatHistory(partnerId, 'NEWER'), 100);
            } else if (ChatState.isReturningToPresent && ChatState.currentPage === 0) {
                ChatState.isReturningToPresent = false;
                // Final scroll to bottom
                if (DOM.messageList) {
                    DOM.messageList.scrollTo({ top: DOM.messageList.scrollHeight, behavior: 'smooth' });
                }
            }
        }
    } catch (error) {
        console.error('[History] Error:', error);
    } finally {
        if (requestId === ChatState.loadingRequestId) {
            ChatState.isLoadingHistory = false;
            removeTopLoadingIndicator();
        }
    }
}

/**
 * Shows a temporary loading indicator at the top of the message list.
 */
function showTopLoadingIndicator() {
    if (!DOM.messageList || document.getElementById('top-loading-indicator')) return;
    const indicator = document.createElement('div');
    indicator.id = 'top-loading-indicator';
    indicator.className = 'chat-loading-mini';
    indicator.innerHTML = '<i class="fas fa-spinner fa-spin"></i>';
    DOM.messageList.prepend(indicator);
}

/**
 * Removes the top loading indicator.
 */
function removeTopLoadingIndicator() {
    const indicator = document.getElementById('top-loading-indicator');
    if (indicator) indicator.remove();
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
const RenderMode = {
    INITIAL: 'INITIAL',   // Append + Scroll to Bottom
    PREPEND: 'PREPEND',   // Prepend + Maintain relative scroll
    APPEND: 'APPEND'     // Append + No forceful scroll (for loading newer)
};

/**
 * Batch render messages.
 * @param {Array} messages - Message DTOs
 * @param {HTMLElement} container - Target container
 * @param {string} mode - RenderMode
 */
function renderMessagesBatch(messages, container, mode = RenderMode.INITIAL) {
    if (!messages || messages.length === 0) return;

    const fragment = document.createDocumentFragment();
    let newMsgCount = 0;

    // Initialize loopLastDate for continuity
    let loopLastDate = null;
    if (mode === RenderMode.APPEND && container) {
        const lastMsg = container.querySelector('.message:last-child');
        if (lastMsg && lastMsg.dataset.timestamp) {
            loopLastDate = getDateString(lastMsg.dataset.timestamp);
        }
    } else if ((mode === RenderMode.INITIAL || mode === RenderMode.PREPEND) && ChatState.hasMoreHistory) {
        // [SEAM-AWARE] If there's more history, don't speculative-render a separator at the top.
        // Initialize loopLastDate with the first message's date to skip its separator.
        if (messages[0] && messages[0].chatTime) {
            loopLastDate = getDateString(messages[0].chatTime);
        }
    }

    messages.forEach(msg => {
        // Skip duplicates (O(1) check)
        if (msg.messageId && ChatState.messageIds.has(msg.messageId)) {
            return;
        }

        // Add to cache
        if (msg.messageId) ChatState.messageIds.add(msg.messageId);
        newMsgCount++;

        // Date Separator Logic (Fires on date change)
        if (msg.chatTime) {
            const msgDate = getDateString(msg.chatTime);
            if (msgDate && msgDate !== loopLastDate) {
                fragment.appendChild(createDateSeparator(msg.chatTime));
                loopLastDate = msgDate;
            }
        }

        const isSentByMe = (msg.senderId === ChatState.currentUserId);
        const reportStatus = msg.reportStatus || 0;
        const msgEl = createMessageElement(msg.content, isSentByMe, msg.senderName, msg.messageId, msg.replyToContent, msg.replyToSenderName, msg.isRead, reportStatus, msg.chatTime);
        fragment.appendChild(msgEl);
    });

    if (newMsgCount === 0 && mode !== RenderMode.INITIAL) return;

    if (mode === RenderMode.INITIAL) {
        if (container) {
            container.innerHTML = '';
            container.appendChild(fragment);
            container.scrollTop = container.scrollHeight;
        }
    }
    else if (mode === RenderMode.PREPEND && container) {
        // Prepend: maintain scroll position relative to existing content
        const oldFirstMsg = container.querySelector('.message');
        const oldScrollHeight = container.scrollHeight;

        // [SEAM-AWARE] Junction Handling
        if (oldFirstMsg) {
            const batchEndDate = loopLastDate; // Date of the last message in prepended batch
            const domStartDate = getDateString(oldFirstMsg.dataset.timestamp);

            if (batchEndDate && domStartDate && batchEndDate !== domStartDate) {
                // Date changed at the seam: Add separator for Day B before prepending
                fragment.appendChild(createDateSeparator(oldFirstMsg.dataset.timestamp));
            } else {
                // Dates match at the seam: Ensure no orphan separator between them
                const maybeSep = oldFirstMsg.previousElementSibling;
                if (maybeSep && maybeSep.classList.contains('date-separator')) {
                    maybeSep.remove();
                }
            }
        }

        container.insertBefore(fragment, container.firstChild);

        const newScrollHeight = container.scrollHeight;
        container.scrollTop = newScrollHeight - oldScrollHeight;
    }
    else if (mode === RenderMode.APPEND && container) {
        container.appendChild(fragment);
    }
}

/**
 * Create message DOM element (XSS-safe via createTextNode).
 * Added chatTime parameter
 */
function createMessageElement(content, isSent, senderName, messageId, replyToContent, replyToSenderName, isRead, reportStatus = 0, chatTime = null) {
    const msgDiv = document.createElement('div');
    msgDiv.className = 'message ' + (isSent ? 'sent' : 'received');

    if (messageId) {
        msgDiv.dataset.messageId = messageId;
        msgDiv.dataset.reportStatus = reportStatus; // Store status
        if (chatTime) msgDiv.dataset.timestamp = chatTime; // for date comparison
        msgDiv.style.cursor = 'pointer';

        // Left click: Toggle Reply
        msgDiv.addEventListener('click', function () {
            // Ignore if user is selecting text
            if (window.getSelection().toString().length > 0) return;
            toggleReply(msgDiv, messageId, content, senderName);
        });

        // Right click: Context Menu
        msgDiv.addEventListener('contextmenu', function (e) {
            e.preventDefault();
            showContextMenu(e, messageId, reportStatus);
        });

        // Add visual indicator if reported (Status 1: Pending, 3: Rejected)
        // Status 2 is Hidden, so handling it separately below
        if (reportStatus == 1 || reportStatus == 3) {
            const reportIcon = document.createElement('i');
            reportIcon.className = 'fas fa-flag';
            reportIcon.style.cssText = 'font-size: 10px; color: #e74c3c; position: absolute; top: -5px; right: 5px; background:white; border-radius:50%; padding:2px; box-shadow:0 1px 2px rgba(0,0,0,0.1);';
            msgDiv.appendChild(reportIcon);
        }
    }

    // Handle Hidden Message (Status 2: Processed)
    if (reportStatus == 2) {
        msgDiv.classList.add('hidden-message');
        msgDiv.style.fontStyle = 'italic';
        msgDiv.style.color = '#888';
        content = "此訊息因違反社群規範已被隱藏";
    }

    // Reply context (if this message is a reply)
    if (replyToContent && reportStatus !== 2) { // Hide reply context if message is hidden
        const contextDiv = document.createElement('div');
        contextDiv.className = 'reply-context';

        const nameStrong = document.createElement('strong');
        nameStrong.textContent = replyToSenderName + ': ';
        contextDiv.appendChild(nameStrong);

        const contentSpan = document.createElement('span');
        contentSpan.textContent = truncateText(replyToContent, 30);
        contextDiv.appendChild(contentSpan);

        msgDiv.appendChild(contextDiv);
    }

    // Message body (XSS-safe)
    msgDiv.appendChild(document.createTextNode(content));

    // Message Timestamp
    if (chatTime) {
        const timeSpan = document.createElement('span');
        timeSpan.className = 'message-time';
        timeSpan.textContent = formatTime(chatTime);
        msgDiv.appendChild(timeSpan);
    }

    // Precise Read Status Rendering
    if (isSent && isRead) {
        const span = document.createElement('span');
        span.className = 'read-status';
        span.textContent = '已讀';
        msgDiv.appendChild(span);
    }

    return msgDiv;
}


/**
 * Append single message and smart-scroll.
 */
function appendMessage(content, isSent, senderName, messageId, replyToContent, replyToSenderName, isRead = false, reportStatus = 0, chatTime = new Date().toISOString()) {
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

    const msgDiv = createMessageElement(content, isSent, senderName, messageId, replyToContent, replyToSenderName, isRead, reportStatus, chatTime); // New messages are unread by default

    // [FIX] Check if we need a date separator (Reliable check against last message)
    if (chatTime) {
        const lastMessage = DOM.messageList.querySelector('.message:last-child');
        if (lastMessage && lastMessage.dataset.timestamp) {
            const lastDate = getDateString(lastMessage.dataset.timestamp);
            const currentDate = getDateString(chatTime);
            if (lastDate !== currentDate) {
                DOM.messageList.appendChild(createDateSeparator(chatTime));
            }
        } else if (DOM.messageList.children.length === 0) {
            // First message ever
            DOM.messageList.appendChild(createDateSeparator(chatTime));
        }
    }

    // Smart scroll: only auto-scroll if user is near bottom or just sent a message
    const isNearBottom = (DOM.messageList.scrollHeight - DOM.messageList.scrollTop - DOM.messageList.clientHeight) < CONFIG.SCROLL_THRESHOLD_PX;
    DOM.messageList.appendChild(msgDiv);

    if (isSent || isNearBottom) {
        DOM.messageList.scrollTop = DOM.messageList.scrollHeight;
    }
}

// ============================================================
// INFINITE SCROLL & JUMP CONTROL
// ============================================================
function attachScrollListener(element, partnerId) {
    element.onscroll = function () {
        // 1. Load Older (Top)
        if (element.scrollTop === 0 && ChatState.hasMoreHistory && !ChatState.isLoadingHistory) {
            loadChatHistory(partnerId, 'OLDER');
        }

        // 2. Load Newer (Bottom)
        if (ChatState.currentPage > 0 && !ChatState.isLoadingHistory) {
            if (element.scrollTop + element.clientHeight >= element.scrollHeight - 50) {
                loadChatHistory(partnerId, 'NEWER');
            }
        }

        // 3. Show/Hide "Back to Present" Button
        toggleBackToPresentBtn();
    };
}

function toggleBackToPresentBtn() {
    let btn = document.getElementById('btn-back-present');
    if (!btn) {
        btn = document.createElement('button');
        btn.id = 'btn-back-present';
        btn.innerHTML = '<i class="fas fa-arrow-down"></i>'; // Telegram style arrow
        btn.className = 'btn-back-present';
        btn.onclick = () => ChatApp.jumpToPresent();
        const main = document.querySelector('.chat-main');
        if (main) main.appendChild(btn);
    }

    if (!DOM.messageList) return;

    // Show if scrolled up significantly OR if viewing history
    const isScrolledUp = DOM.messageList.scrollTop + DOM.messageList.clientHeight < DOM.messageList.scrollHeight - 300;
    const isDeepInHistory = ChatState.currentPage > 0;

    if (isScrolledUp || isDeepInHistory) {
        btn.style.display = 'flex';
    } else {
        btn.style.display = 'none';
    }
}

function jumpToPresent() {
    console.log('[ChatApp] jumpToPresent clicked, room:', ChatState.currentChatroomId, 'page:', ChatState.currentPage);
    if (!ChatState.currentChatroomId) {
        console.warn('[ChatApp] Cannot jump: no current room ID');
        return;
    }

    if (ChatState.isSearchMode) {
        window.ChatApp.toggleSearchMode();
    }

    if (ChatState.currentPage === 0) {
        console.log('[ChatApp] Simple scroll to bottom');
        DOM.messageList.scrollTo({ top: DOM.messageList.scrollHeight, behavior: 'smooth' });
    } else {
        console.log('[ChatApp] Starting sequential return from page:', ChatState.currentPage);
        ChatState.isReturningToPresent = true;
        ChatState.currentPage--;
        loadChatHistory(ChatState.currentChatroomId, 'NEWER');
    }
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
    DOM.replyToText.textContent = truncateText(content, 30);
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
// REPORTING LOGIC
// ============================================================
function showContextMenu(e, messageId, status) {
    if (!DOM.contextMenu) return;

    ChatState.contextMenuTargetId = messageId;
    ChatState.contextMenuTargetStatus = parseInt(status, 10);

    // Position menu
    DOM.contextMenu.style.display = 'block';
    DOM.contextMenu.style.left = e.pageX + 'px';
    DOM.contextMenu.style.top = e.pageY + 'px';

    // Update Menu Option State
    const btn = DOM.ctxReportBtn;
    if (ChatState.contextMenuTargetStatus === 1) {
        btn.innerHTML = '<i class="fas fa-clock"></i> 已檢舉 (待審核)';
        btn.classList.add('disabled');
        btn.onclick = null;
    } else if (ChatState.contextMenuTargetStatus === 2) {
        btn.innerHTML = '<i class="fas fa-ban"></i> 此訊息已隱藏';
        btn.classList.add('disabled');
        btn.onclick = null;
    } else if (ChatState.contextMenuTargetStatus === 3) {
        btn.innerHTML = '<i class="fas fa-check-circle"></i> 檢舉已駁回 (已結案)';
        btn.classList.add('disabled'); // Prevent re-report if rejected? User request says "cannot report second time"
        btn.onclick = null;
    } else {
        btn.innerHTML = '<i class="fas fa-flag"></i> 檢舉此訊息';
        btn.classList.remove('disabled');
        btn.onclick = () => openReportModal();
    }
}

function openReportModal() {
    if (DOM.reportModal) {
        DOM.reportModal.style.display = 'flex';
        // Reset form
        DOM.reportReasonType.value = "0";
        DOM.reportReasonText.value = "";
    }
    // Context menu auto-hides via global click, but force hide here
    if (DOM.contextMenu) DOM.contextMenu.style.display = 'none';
}

function closeReportModal() {
    if (DOM.reportModal) DOM.reportModal.style.display = 'none';
}

function submitReport() {
    const messageId = ChatState.contextMenuTargetId;
    const type = parseInt(DOM.reportReasonType.value, 10);
    const reason = DOM.reportReasonText.value.trim();

    if (!messageId) return;
    if (!reason) {
        alert("請填寫檢舉說明");
        return;
    }

    fetch('/api/chatrooms/report', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
            messageId: messageId,
            type: type,
            reason: reason
        })
    })
        .then(response => {
            if (response.ok) {
                alert("檢舉已送出，我們將盡快處理。");
                closeReportModal();
                // Optimistically update UI
                updateMessageReportStatus(messageId, 1); // 1 = Pending
            } else if (response.status === 409) {
                alert("您已檢舉過此訊息。");
                closeReportModal();
                updateMessageReportStatus(messageId, 1);
            } else {
                alert("檢舉失敗，請稍後再試。");
            }
        })
        .catch(err => {
            console.error("Report error:", err);
            alert("網路錯誤");
        });
}

function updateMessageReportStatus(messageId, status) {
    const msgDiv = document.querySelector(`.message[data-message-id="${messageId}"]`);
    if (msgDiv) {
        msgDiv.dataset.reportStatus = status;

        // Add icon if not exists
        if (!msgDiv.querySelector('.fa-flag')) {
        }
    }
}

// ============================================================
// DATE & TIME HELPERS
// ============================================================

function createDateSeparator(isoString) {
    const div = document.createElement('div');
    div.className = 'date-separator';
    div.dataset.dateLabel = getDateString(isoString); // Critical for merging logic
    div.textContent = formatDate(isoString);
    return div;
}

function formatTime(isoString) {
    if (!isoString) return '';
    const date = new Date(isoString);
    return date.toLocaleTimeString([], CONFIG.TIME_FORMAT_OPTIONS); // "14:30"
}

function formatDate(isoString) {
    const date = new Date(isoString);
    if (!isoString || isNaN(date.getTime())) return '';

    const today = new Date();
    const yesterday = new Date();
    yesterday.setDate(yesterday.getDate() - 1);

    if (date.toDateString() === today.toDateString()) {
        return 'Today';
    } else if (date.toDateString() === yesterday.toDateString()) {
        return 'Yesterday';
    } else {
        return date.toLocaleDateString([], CONFIG.DATE_FORMAT_OPTIONS).replace(/\//g, '-');
    }
}

function getDateString(isoString) {
    if (!isoString) return null;
    const d = new Date(isoString);
    if (isNaN(d.getTime())) return null;
    return d.toDateString();
}

function truncateText(text, maxLength) {
    if (!text) return '';
    if (text.length <= maxLength) return text;
    return text.substring(0, maxLength) + '...';
}

function renderSearchResults(messages, keyword) {
    const container = document.getElementById('search-results-list');
    container.innerHTML = '';

    if (messages.length === 0) {
        container.innerHTML = '<div style="padding:2rem; text-align:center; color:#999;"><i class="fas fa-search" style="font-size:2rem;margin-bottom:1rem;display:block;"></i>找不到符合的訊息</div>';
        return;
    }

    // Add header
    const header = document.createElement('div');
    header.style.padding = '0 0.5rem 1rem';
    header.style.color = '#666';
    header.style.fontSize = '0.9rem';
    header.textContent = `找到 ${messages.length} 則相關訊息`;
    container.appendChild(header);

    messages.forEach(msg => {
        const div = document.createElement('div');
        div.className = 'search-result-item'; // Use new CSS class
        div.onclick = () => ChatApp.jumpToMessage(msg.messageId);

        // Date formatting
        const date = new Date(msg.chatTime);
        const timeStr = date.toLocaleString('zh-TW', { month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' });

        // Highlight keyword (Safe)
        const content = msg.content || '';
        const regex = new RegExp(`(${keyword})`, 'gi');
        // Simple replace is insufficient for HTML if content contains tags, but MVP assumes text
        const highlightedContent = content.replace(regex, '<mark>$1</mark>');

        div.innerHTML = `
            <div class="search-result-meta">
                <span class="search-result-sender">${msg.senderName}</span>
                <span>${timeStr}</span>
            </div>
            <div class="search-result-content">${highlightedContent}</div>
        `;
        container.appendChild(div);
    });
}




// ============================================================
// SEARCH & JUMP LOGIC
// ============================================================
/**
 * Toggle between Message List and Search Results.
 * @param {boolean} forceValue - Optional specific state to force
 * @param {boolean} immediate - If true, bypass animation delays for jumps
 */
function toggleSearchMode(forceValue = null, immediate = false) {
    if (forceValue !== null) {
        ChatState.isSearchMode = forceValue;
    } else {
        ChatState.isSearchMode = !ChatState.isSearchMode;
    }

    const searchBar = document.getElementById('chat-search-bar');
    const headerTools = document.getElementById('header-tools');
    const searchResults = document.getElementById('search-results-list');
    const messageList = document.getElementById('message-list');
    const input = document.getElementById('msg-search-input');

    if (!searchBar || !headerTools || !searchResults || !messageList || !input) return;

    if (ChatState.isSearchMode) {
        searchBar.classList.add('active');
        headerTools.style.display = 'none';
        searchResults.style.display = 'block';
        messageList.style.display = 'none';

        if (immediate) {
            input.focus();
        } else {
            setTimeout(() => input.focus(), 300);
        }
    } else {
        searchBar.classList.remove('active');

        const cleanup = () => {
            headerTools.style.display = 'block';
            searchResults.style.display = 'none';
            messageList.style.display = 'flex';
            searchResults.innerHTML = '';
        };

        if (immediate) {
            cleanup();
        } else {
            setTimeout(cleanup, 300);
        }

        input.value = '';
    }
}

function handleSearchKeyPress(event) {
    if (event.code === 'Enter' || event.key === 'Enter') {
        searchHistory();
    }
}

function searchHistory() {
    const input = document.getElementById('msg-search-input');
    const keyword = input ? input.value.trim() : '';

    if (!keyword) return;
    if (!ChatState.currentChatroomId) return;

    const url = `/api/chatrooms/${ChatState.currentChatroomId}/messages/search?keyword=${encodeURIComponent(keyword)}`;

    fetch(url)
        .then(response => {
            if (!response.ok) throw new Error('Search failed');
            return response.json();
        })
        .then(data => {
            renderSearchResults(data, keyword);
        })
        .catch(err => console.error('Search error:', err));
}

async function jumpToMessage(messageId) {
    if (!ChatState.currentChatroomId) return;

    ChatState.isLoadingHistory = true;
    toggleSearchMode(false, true); // Immediate UI Swap to prevent scroll view issues

    const url = `/api/chatrooms/${ChatState.currentChatroomId}/messages/${messageId}/position?size=${CONFIG.PAGE_SIZE}`;

    try {
        const posRes = await fetch(url);
        const pos = await posRes.json();
        const targetPage = pos.page;

        // Reset DOM and ID cache for clean render
        if (DOM.messageList) {
            DOM.messageList.innerHTML = '<div class="chat-empty-state"><i class="fas fa-spinner fa-spin"></i><p>Loading...</p></div>';
            ChatState.messageIds.clear();
        }

        const msgRes = await fetch(`/api/chatrooms/${ChatState.currentChatroomId}/messages?userId=${ChatState.currentUserId}&page=${targetPage}&size=${CONFIG.PAGE_SIZE}`);
        if (!msgRes.ok) throw new Error('Failed to fetch target page');

        const messages = await msgRes.json();

        // Commit state
        ChatState.currentPage = targetPage;
        ChatState.hasMoreHistory = (messages.length === CONFIG.PAGE_SIZE);

        // Render (Treat as INITIAL for clear jump, but we've already cleared DOM)
        renderMessagesBatch(messages, DOM.messageList, RenderMode.INITIAL);

        // [TRUE SMOOTH JUMP] Wait for browser layout
        requestAnimationFrame(() => {
            requestAnimationFrame(() => {
                const targetEl = document.querySelector(`[data-message-id="${messageId}"]`);
                if (targetEl) {
                    targetEl.scrollIntoView({ behavior: 'smooth', block: 'center' });
                    // Highlight effect
                    targetEl.style.transition = 'background-color 0.5s';
                    targetEl.style.backgroundColor = '#fffec8';
                    setTimeout(() => targetEl.style.backgroundColor = '', 2000);
                } else {
                    console.warn('[Jump] Target message element not found in DOM after render:', messageId);
                }
            });
        });

    } catch (err) {
        console.error('[Jump] Error:', err);
    } finally {
        ChatState.isLoadingHistory = false;
    }
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
    updateSendButton,
    // Search public access
    toggleSearchMode,
    handleSearchKeyPress,
    searchHistory,
    jumpToMessage,
    // Bottom Scroll public access
    jumpToPresent,
    toggleBackToPresentBtn,
    // Report public access
    openReportModal,
    closeReportModal,
    submitReport
};

document.addEventListener('DOMContentLoaded', initChat);
