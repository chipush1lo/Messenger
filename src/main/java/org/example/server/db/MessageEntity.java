package org.example.server.db;

import jakarta.persistence.*;
import org.example.protocol.MessageType;
import java.time.LocalDateTime;

@Entity
@Table(name = "messages")
public class MessageEntity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MessageType type;

    @Column(nullable = false)
    private String sender;

    private String receiver;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private LocalDateTime timestamp;

    private Long replyToId;
    private String replyToContent;
    private String fileName;

    @Column(columnDefinition = "TEXT")
    private String fileDataBase64;

    public MessageEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    
    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }
    
    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }
    
    public String getReceiver() { return receiver; }
    public void setReceiver(String receiver) { this.receiver = receiver; }
    
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    
    public LocalDateTime getTimestamp() { return timestamp; }
    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public Long getReplyToId() { return replyToId; }
    public void setReplyToId(Long replyToId) { this.replyToId = replyToId; }

    public String getReplyToContent() { return replyToContent; }
    public void setReplyToContent(String replyToContent) { this.replyToContent = replyToContent; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileDataBase64() { return fileDataBase64; }
    public void setFileDataBase64(String fileDataBase64) { this.fileDataBase64 = fileDataBase64; }
}
