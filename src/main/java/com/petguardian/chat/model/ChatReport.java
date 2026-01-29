package com.petguardian.chat.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.petguardian.admin.model.Admin;
import com.petguardian.member.model.Member;
import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * Entity: Chat Message Report.
 * Maps to 'report' table.
 */
@Entity
@Table(name = "report", uniqueConstraints = {
        @UniqueConstraint(columnNames = { "reporter_id", "message_id" })
})
@Data
@NoArgsConstructor
public class ChatReport {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "report_id")
    private Integer reportId;

    @Column(name = "reporter_id")
    private Integer reporterId;

    // Optional: ManyToOne relationship if we want to access reporter details
    // directly
    // Using insertable=false, updatable=false to avoid conflict with reporterId
    // column if desired,
    // or just map relation directly. Here we map relation directly and use
    // @JoinColumn.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter_id", insertable = false, updatable = false)
    @JsonIgnore
    private Member reporter;

    @Column(name = "message_id", length = 13, nullable = false)
    private String messageId;

    // Relation to ChatMessageEntity for retrieving content
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "message_id", insertable = false, updatable = false)
    @JsonIgnore
    private ChatMessageEntity message;

    @Column(name = "report_type")
    private Integer reportType; // 0:Harassment, 1:Spam, 2:Inappropriate, 3:Fraud, 4:Other

    @Column(name = "report_reason", length = 500)
    private String reportReason;

    /**
     * Report Status:
     * 0: Pending (待處理)
     * 1: Processing (處理中) - Reserved for future use
     * 2: Processed/Closed (已處理)
     * 3: Rejected (已駁回)
     */
    @Column(name = "report_status")
    private Integer reportStatus = 0;

    @CreationTimestamp
    @Column(name = "report_time", updatable = false)
    private LocalDateTime reportTime;

    @Column(name = "handler_id")
    private Integer handlerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "handler_id", insertable = false, updatable = false)
    @JsonIgnore
    private Admin handler;

    @Column(name = "handle_time")
    private LocalDateTime handleTime;

    @Column(name = "handle_note", length = 100)
    private String handleNote;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
