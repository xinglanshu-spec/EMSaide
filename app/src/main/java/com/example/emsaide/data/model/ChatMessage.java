package com.example.emsaide.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * 聊天消息实体类
 * 存储与每个联系人的聊天消息
 */
@Entity(tableName = "chat_messages")
public class ChatMessage {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    /** 关联的对话 ID */
    private long conversationId;
    
    /** 关联的邮箱账户 ID（用于发送） */
    private long accountId;
    
    /** 消息类型：RECEIVED(接收), SENT(发送), SYSTEM(系统) */
    private MessageType type;
    
    /** 消息内容 */
    private String content;
    
    /** 邮件主题（如果是邮件消息） */
    private String subject;
    
    /** 发件人 */
    private String sender;
    
    /** 收件人 */
    private String receiver;
    
    /** 消息时间戳 */
    private long timestamp;
    
    /** 是否已读 */
    private boolean isRead;
    
    /** 原始邮件 ID（用于删除等操作） */
    private String emailMessageId;

    public ChatMessage() {
        this.timestamp = System.currentTimeMillis();
    }

    public enum MessageType {
        RECEIVED,  // 接收的邮件
        SENT,      // 发送的邮件
        SYSTEM     // 系统消息
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getConversationId() {
        return conversationId;
    }

    public void setConversationId(long conversationId) {
        this.conversationId = conversationId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public MessageType getType() {
        return type;
    }

    public void setType(MessageType type) {
        this.type = type;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public boolean isRead() {
        return isRead;
    }

    public void setRead(boolean read) {
        isRead = read;
    }

    public String getEmailMessageId() {
        return emailMessageId;
    }

    public void setEmailMessageId(String emailMessageId) {
        this.emailMessageId = emailMessageId;
    }
}
