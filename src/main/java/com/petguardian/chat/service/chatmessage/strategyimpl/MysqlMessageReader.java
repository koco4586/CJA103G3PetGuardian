package com.petguardian.chat.service.chatmessage.strategyimpl;

import com.petguardian.chat.model.ChatMessageEntity;
import com.petguardian.chat.model.ChatMessageRepository;
import com.petguardian.chat.service.chatmessage.MessageReaderStrategyService;
import com.petguardian.chat.service.chatmessage.ResilienceSupportStrategyService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

/**
 * MySQL Implementation of Message Reading.
 */
@Service("mysqlMessageReader")
public class MysqlMessageReader implements MessageReaderStrategyService, ResilienceSupportStrategyService {

    private final ChatMessageRepository messageRepository;

    public MysqlMessageReader(ChatMessageRepository messageRepository) {
        this.messageRepository = messageRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageEntity> findLatestMessages(Integer chatroomId, Pageable pageable) {
        return messageRepository.findLatest(chatroomId, pageable);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ChatMessageEntity> findById(String messageId) {
        return messageRepository.findById(messageId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ChatMessageEntity> findAllById(Iterable<String> messageIds) {
        return messageRepository.findAllById(messageIds);
    }

    @Override
    public boolean isHealthy() {
        return true; // Usually handled by DataSource check in Writer
    }
}
