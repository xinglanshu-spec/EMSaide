package com.example.emsaide.data.model;

import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;

/**
 * 对话实体类
 * 每个对话关联一个联系人和一个发件邮箱
 */
@Entity(tableName = "conversations",
    foreignKeys = {
        @ForeignKey(entity = Contact.class,
            parentColumns = "id",
            childColumns = "contactId",
            onDelete = ForeignKey.CASCADE),
        @ForeignKey(entity = EmailAccount.class,
            parentColumns = "id",
            childColumns = "accountId",
            onDelete = ForeignKey.CASCADE)
    },
    indices = {@Index("contactId"), @Index("accountId")}
)
public class Conversation {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    /** 联系人 ID */
    private long contactId;
    
    /** 使用的邮箱账户 ID */
    private long accountId;
    
    /** 最后一条消息内容 */
    private String lastMessage;
    
    /** 最后一条消息时间 */
    private long lastMessageTime;
    
    /** 未读消息数 */
    private int unreadCount;
    
    /** 创建时间 */
    private long createdAt;

    public Conversation() {
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getContactId() {
        return contactId;
    }

    public void setContactId(long contactId) {
        this.contactId = contactId;
    }

    public long getAccountId() {
        return accountId;
    }

    public void setAccountId(long accountId) {
        this.accountId = accountId;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public long getLastMessageTime() {
        return lastMessageTime;
    }

    public void setLastMessageTime(long lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }
}
