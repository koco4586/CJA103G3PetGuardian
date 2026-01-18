/**
 * Chat MVP Module
 * Handles WebSocket integration, state management, and UI rendering.
 */


// --- State Container ---
const ChatState = {
    stompClient: null,
    currentUserId: null,
    currentUserName: null,
    selectedUserId: null,
    selectedUserName: null,
    currentReplyId: null,
    loadingRequestId: 0, // Race condition guard
    currentPage: 0,
    isLoadingHistory: false,
    hasMoreHistory: true
};

/**
 * Initialization
 */
function initChat() {
    const userIdVal = document.getElementById('currentUserId').value;
    const userNameVal = document.getElementById('currentUserName').value;

    ChatState.currentUserId = parseInt(userIdVal, 10);
    ChatState.currentUserName = userNameVal;

    console.log(`[Init] User: ${ChatState.currentUserName} (${ChatState.currentUserId})`);

    connect();
}

/**
 * WebSocket Connection (SockJS + STOMP)
 */
function connect() {
    const socket = new SockJS('/ws');
    ChatState.stompClient = Stomp.over(socket);



    ChatState.stompClient.connect({}, function (frame) {
        console.log('[WS] Connected');

        // Topic Subscription
        ChatState.stompClient.subscribe('/topic/messages.' + ChatState.currentUserId, function (message) {
            onMessageReceived(JSON.parse(message.body));
        });

    }, function (error) {
        console.error('[WS] Error:', error);
        setTimeout(connect, 5000); // Reconnection strategy
    });
}


/**
 * Message Event Handler
 */
function onMessageReceived(dto) {
    console.log('[Msg] Received:', dto);

    const senderId = parseInt(dto.senderId, 10);
    const receiverId = parseInt(dto.receiverId, 10);
    const isSentByMe = (senderId === ChatState.currentUserId);

    // View Context Logic
    let shouldAppend = false;

    if (ChatState.selectedUserId) {
        if (isSentByMe) {
            shouldAppend = (receiverId === ChatState.selectedUserId);
        } else {
            shouldAppend = (senderId === ChatState.selectedUserId); // Incoming match
        }
    }

    // Determine UI Partner Context
    const partnerId = isSentByMe ? receiverId : senderId;

    if (shouldAppend) {
        appendMessage(dto.content, isSentByMe, dto.senderName, dto.messageId, dto.replyToContent, dto.replyToSenderName);
    } else {
        // Notification Logic (Red Dot)
        if (!isSentByMe) {
            const contactItem = document.querySelector(`.contact-item[data-user-id="${partnerId}"]`);
            if (contactItem) {
                const dot = contactItem.querySelector('.unread-dot');
                if (dot) dot.classList.remove('app-d-none');
            }
        }
    }

    // Sidebar Sync
    updateContactLastMessage(partnerId, dto.content);
}

/**
 * Update Sidebar Preview
 */
function updateContactLastMessage(userId, content) {
    const contactItem = document.querySelector(`.contact-item[data-user-id="${userId}"]`);
    if (contactItem) {
        const msgDiv = contactItem.querySelector('.contact-last-msg');
        if (msgDiv) msgDiv.textContent = content;
    }
}

/**
 * User Selection Handler
 */
function selectUser(userId, userName) {
    ChatState.selectedUserId = parseInt(userId, 10);
    ChatState.selectedUserName = userName;

    console.log(`[Select] ${userName} (${userId})`);

    // UI Update: Active State
    document.querySelectorAll('.contact-item').forEach(item => {
        item.classList.remove('active');
        if (parseInt(item.dataset.userId, 10) === ChatState.selectedUserId) {
            item.classList.add('active');

            // Clear Notification
            const dot = item.querySelector('.unread-dot');
            if (dot) dot.classList.add('app-d-none');
        }
    });

    // Update Header
    const headerTitle = document.getElementById('chatHeaderTitle');
    if (headerTitle) {
        headerTitle.textContent = userName;
    }

    // Reset View
    const messageList = document.getElementById('message-list');
    messageList.innerHTML = '<div class="chat-empty-state"><i class="fas fa-spinner fa-spin"></i><p>Loading...</p></div>';

    document.getElementById('chat-input-form').style.display = 'flex';

    loadChatHistory(userId);
}

/**
 * Fetch Chat History (Paginated)
 */
function loadChatHistory(partnerId, isLoadMore = false) {
    if (!isLoadMore) {
        ChatState.currentPage = 0;
        ChatState.hasMoreHistory = true;
        ChatState.loadingRequestId++; // Reset Context
    }

    const requestId = ChatState.loadingRequestId;
    ChatState.isLoadingHistory = true;

    const partnerIdInt = parseInt(partnerId, 10);
    const page = ChatState.currentPage;
    const size = 50;

    console.log(`[History] Req:${requestId} Partner:${partnerIdInt} Page:${page}`);

    // 1. Resolve Chatroom
    fetch(`/api/chatrooms?partnerId=${partnerIdInt}&userId=${ChatState.currentUserId}`)
        .then(response => {
            if (requestId !== ChatState.loadingRequestId) return null;
            if (response.status === 404) return null;
            if (!response.ok) throw new Error('Chatroom lookup failed');
            return response.json();
        })
        .then(chatroom => {
            if (requestId !== ChatState.loadingRequestId) return;
            const messageList = document.getElementById('message-list');

            if (!chatroom) {
                messageList.innerHTML = `<div class="chat-empty-state"><i class="fas fa-comments"></i><p>Start chatting with ${ChatState.selectedUserName}</p></div>`;
                ChatState.isLoadingHistory = false;
                return;
            }

            // 2. Fetch Messages
            return fetch(`/api/chatrooms/${chatroom.chatroomId}/messages?userId=${ChatState.currentUserId}&page=${page}&size=${size}`)
                .then(response => {
                    if (requestId !== ChatState.loadingRequestId) return null;
                    if (!response.ok) throw new Error('Message fetch failed');
                    return response.json();
                })
                .then(messages => {
                    if (requestId !== ChatState.loadingRequestId) return;

                    ChatState.isLoadingHistory = false;
                    if (messages.length < size) {
                        ChatState.hasMoreHistory = false;
                    }

                    if (!isLoadMore) {
                        // Initial Load
                        messageList.innerHTML = '';
                        if (messages.length === 0) {
                            messageList.innerHTML = `<div class="chat-empty-state"><i class="fas fa-comments"></i><p>Start chatting with ${ChatState.selectedUserName}</p></div>`;
                        } else {
                            renderMessagesPro(messages, messageList, true);
                            attachScrollListener(messageList, partnerIdInt);
                        }
                    } else {
                        // Pagination Load (Prepend)
                        if (messages.length > 0) {
                            renderMessagesPro(messages, messageList, false);
                        }
                    }
                });
        })
        .catch(error => {
            if (requestId !== ChatState.loadingRequestId) return;
            console.error('[History] Error:', error);
            ChatState.isLoadingHistory = false;
            if (!isLoadMore) {
                const messageList = document.getElementById('message-list');
                messageList.innerHTML = '<div class="chat-empty-state"><i class="fas fa-exclamation-triangle"></i><p>Load failed</p></div>';
            }
        });
}

/**
 * Message Renderer (DocumentFragment optimized)
 * Handles both append (real-time) and prepend (history).
 */
function renderMessagesPro(messages, container, scrollToBottom) {
    const fragment = document.createDocumentFragment();

    messages.forEach(msg => {
        // Duplicate Check
        if (msg.messageId && container.querySelector(`.message[data-message-id="${msg.messageId}"]`)) {
            return;
        }

        const isSentByMe = (msg.senderId === ChatState.currentUserId);
        const msgEl = createMessageElement(msg.content, isSentByMe, msg.senderName, msg.messageId, msg.replyToContent, msg.replyToSenderName);
        fragment.appendChild(msgEl);
    });

    if (scrollToBottom) {
        container.appendChild(fragment);
        container.scrollTop = container.scrollHeight;
    } else {
        // Prepend Logic (Maintain Scroll Position)
        const oldScrollHeight = container.scrollHeight;
        container.insertBefore(fragment, container.firstChild);
        const newScrollHeight = container.scrollHeight;
        container.scrollTop = newScrollHeight - oldScrollHeight;
    }
}

/**
 * Infinite Scroll Observer
 */
function attachScrollListener(element, partnerId) {
    element.onscroll = function () {
        if (element.scrollTop === 0 && ChatState.hasMoreHistory && !ChatState.isLoadingHistory) {
            console.log('[Scroll] Loading more...');
            ChatState.currentPage++;
            loadChatHistory(partnerId, true);
        }
    };
}

/**
 * Message Dispatcher
 */
function sendMessage() {
    const input = document.getElementById('msg-input');
    const content = input.value.trim();

    if (content === '' || !ChatState.selectedUserId) {
        if (!ChatState.selectedUserId) {
            alert('Select a user first');
        }
        return;
    }

    const dto = {
        senderId: ChatState.currentUserId,
        receiverId: ChatState.selectedUserId,
        content: content,
        senderName: ChatState.currentUserName,
        replyToId: ChatState.currentReplyId
    };

    ChatState.stompClient.send('/app/chat.send', {}, JSON.stringify(dto));

    // Optimistic UI Update
    updateContactLastMessage(ChatState.selectedUserId, 'You: ' + content);

    cancelReply();

    input.value = '';
    input.focus();
}

/**
 * DOM Element Factory (XSS Safe)
 */
function createMessageElement(content, isSent, senderName, messageId, replyToContent, replyToSenderName) {
    const msgDiv = document.createElement('div');
    msgDiv.className = 'message ' + (isSent ? 'sent' : 'received');
    if (messageId) {
        msgDiv.dataset.messageId = messageId;
    }

    if (messageId) {
        msgDiv.addEventListener('click', function (e) {
            if (window.getSelection().toString().length > 0) return;
            toggleReply(msgDiv, messageId, content, senderName);
        });
        msgDiv.style.cursor = 'pointer';
    }

    // Reply Context
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

    // Message Body
    const textNode = document.createTextNode(content);
    msgDiv.appendChild(textNode);

    return msgDiv;
}

/**
 * Append Message & Smart Scroll
 */
function appendMessage(content, isSent, senderName, messageId, replyToContent, replyToSenderName, autoScroll = true) {
    const messageList = document.getElementById('message-list');

    // Duplicate Guard
    if (messageId && document.querySelector(`.message[data-message-id="${messageId}"]`)) {
        console.warn('[Append] Duplicate prevented:', messageId);
        return;
    }

    // Clear Empty State
    const emptyState = messageList.querySelector('.chat-empty-state');
    if (emptyState) {
        emptyState.remove();
    }

    const msgDiv = createMessageElement(content, isSent, senderName, messageId, replyToContent, replyToSenderName);

    // Smart Scroll Heuristics
    const isNearBottom = (messageList.scrollHeight - messageList.scrollTop - messageList.clientHeight) < 100;

    messageList.appendChild(msgDiv);

    if (autoScroll) {
        if (isSent || isNearBottom) {
            messageList.scrollTop = messageList.scrollHeight;
        } else {
            console.log('[Append] Background update (user active on history)');
        }
    }
}

/**
 * Input Event Handler
 */
function handleKeyPress(event) {
    if (event.key === 'Enter' && !event.shiftKey) {
        event.preventDefault();
        sendMessage();
    }
}
window.handleKeyPress = handleKeyPress;

function updateSendButton() {
    const input = document.getElementById('msg-input');
    const sendBtn = document.getElementById('send-btn');

    if (input.value.trim() !== '') {
        sendBtn.classList.add('active');
    } else {
        sendBtn.classList.remove('active');
    }
}



document.addEventListener('DOMContentLoaded', initChat);

/**
 * Reply Logic
 */
function toggleReply(msgElement, messageId, content, senderName) {
    if (ChatState.currentReplyId === messageId) {
        cancelReply();
        return;
    }

    document.querySelectorAll('.message.reply-selected').forEach(el => el.classList.remove('reply-selected'));

    ChatState.currentReplyId = messageId;
    msgElement.classList.add('reply-selected');

    const previewBar = document.getElementById('reply-preview-bar');
    document.getElementById('reply-to-name').textContent = 'Reply to ' + senderName;
    document.getElementById('reply-to-text').textContent = content;
    previewBar.style.display = 'flex';

    document.getElementById('msg-input').focus();
}

function cancelReply() {
    ChatState.currentReplyId = null;
    document.querySelectorAll('.message.reply-selected').forEach(el => el.classList.remove('reply-selected'));
    document.getElementById('reply-preview-bar').style.display = 'none';
}

/**
 * Contact Search Filter
 */
function filterContacts(query) {
    const contactItems = document.querySelectorAll('.contact-item');
    const lowerQuery = query.toLowerCase();

    contactItems.forEach(item => {
        const userName = item.getAttribute('data-user-name').toLowerCase();
        if (userName.includes(lowerQuery)) {
            item.style.display = '';
        } else {
            item.style.display = 'none';
        }
    });
}


/**
 * Utility: Debounce
 */
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

const debouncedFilterContacts = debounce(filterContacts, 300);

window.filterContacts = debouncedFilterContacts;
