package org.example.protocol;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlRootElement;

import java.time.LocalDateTime;
import java.util.List;

@JacksonXmlRootElement(localName = "message")
@JsonInclude(JsonInclude.Include.NON_NULL)
public class Message {

    private MessageType type;
    private Long id;
    private String sender;
    private String receiver;
    private String content;
    private LocalDateTime timestamp;
    private List<String> userList;
    
    private Long replyToId;
    private String replyToContent;
    private String fileName;
    private String fileDataBase64;

    // Required for Jackson
    public Message() {
        this.timestamp = LocalDateTime.now();
    }

    public Message(MessageType type, String sender, String receiver, String content) {
        this.type = type;
        this.sender = sender;
        this.receiver = receiver;
        this.content = content;
        this.timestamp = LocalDateTime.now();
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getSender() {
        return sender;
    }

    public void setSender(String sender) {
        this.sender = sender;
    }

    public String getReceiver() {
        return receiver;
    }

    public void setReceiver(String receiver) {
        this.receiver = receiver;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }

    public List<String> getUserList() {
        return userList;
    }

    public void setUserList(List<String> userList) {
        this.userList = userList;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public Long getReplyToId() { return replyToId; }
    public void setReplyToId(Long replyToId) { this.replyToId = replyToId; }

    public String getReplyToContent() { return replyToContent; }
    public void setReplyToContent(String replyToContent) { this.replyToContent = replyToContent; }

    public String getFileName() { return fileName; }
    public void setFileName(String fileName) { this.fileName = fileName; }

    public String getFileDataBase64() { return fileDataBase64; }
    public void setFileDataBase64(String fileDataBase64) { this.fileDataBase64 = fileDataBase64; }
}
