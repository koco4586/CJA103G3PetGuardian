package com.petguardian.chat.service.chatmessage.strategyimpl;

import com.petguardian.chat.model.ChatMessageEntity;
import com.petguardian.chat.model.ChatMessageRepository;
import com.petguardian.chat.service.chatmessage.MessageCreationContext;
import com.petguardian.chat.service.chatmessage.MessageWriterStrategyService;
import com.petguardian.chat.service.chatmessage.resilience.ResilienceChatFailureHandler;
import com.petguardian.chat.service.chatroom.ChatRoomMetadataWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.Statement;
import java.util.List;

/**
 * MySQL Implementation of Message Writing.
 * Handles Direct SQL Persistence and Metadata Sync.
 */
@Slf4j
@Service("mysqlMessageWriter")
public class MysqlMessageWriter implements MessageWriterStrategyService {

    private final ChatMessageRepository messageRepository;
    private final ChatRoomMetadataWriter metadataWriter;
    private final DataSource dataSource;
    private final ResilienceChatFailureHandler failureHandler;

    public MysqlMessageWriter(ChatMessageRepository messageRepository,
            @Qualifier("metadataWriterProxy") ChatRoomMetadataWriter metadataWriter,
            DataSource dataSource,
            ResilienceChatFailureHandler failureHandler) {
        this.messageRepository = messageRepository;
        this.metadataWriter = metadataWriter;
        this.dataSource = dataSource;
        this.failureHandler = failureHandler;
    }

    @Override
    @Transactional
    public ChatMessageEntity save(MessageCreationContext context) {
        ChatMessageEntity saved = messageRepository.save(context.toEntity());
        metadataWriter.syncRoomMetadata(context.chatroomId(), context.content(), context.createdAt(),
                context.senderId());
        return saved;
    }

    @Override
    @Transactional
    public void saveAll(List<MessageCreationContext> contexts) {
        if (contexts == null || contexts.isEmpty())
            return;

        log.info("[MySQL-Writer][{}] Persistence Start. Size: {}", Thread.currentThread().getName(), contexts.size());

        List<ChatMessageEntity> entities = contexts.stream().map(MessageCreationContext::toEntity).toList();
        messageRepository.saveAll(entities);

        contexts.stream()
                .collect(java.util.stream.Collectors.toMap(
                        MessageCreationContext::chatroomId,
                        ctx -> ctx,
                        (existing, replacement) -> replacement.createdAt().isAfter(existing.createdAt()) ? replacement
                                : existing))
                .values()
                .forEach(ctx -> metadataWriter.syncRoomMetadata(ctx.chatroomId(), ctx.content(), ctx.createdAt(),
                        ctx.senderId()));
    }

    @Override
    public boolean isHealthy() {
        try (Connection conn = dataSource.getConnection(); Statement stmt = conn.createStatement()) {
            stmt.executeQuery("SELECT 1");
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    @Override
    public boolean isConnectionException(Throwable e) {
        return failureHandler.isConnectionException(e);
    }

    @Override
    public boolean isDataIntegrityViolation(Throwable e) {
        return failureHandler.isDataIntegrityViolation(e);
    }
}
