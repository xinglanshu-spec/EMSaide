package com.example.emsaide.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 联系人实体类
 * 每个联系人是一个聊天对象
 */
@Entity(tableName = "contacts")
public class Contact {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    /** 联系人名称（显示名称） */
    private String name;
    
    /** 联系人邮箱地址 */
    private String email;
    
    /** 头像颜色（用于生成随机头像） */
    private int avatarColor;
    
    /** 备注 */
    private String remark;
    
    /** 创建时间 */
    private long createdAt;
    
    /** 最后聊天时间 */
    private long lastChatTime;

    public Contact() {
        this.createdAt = System.currentTimeMillis();
        // 生成随机颜色用于头像
        this.avatarColor = (int)(Math.random() * 0xFFFFFF);
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public int getAvatarColor() {
        return avatarColor;
    }

    public void setAvatarColor(int avatarColor) {
        this.avatarColor = avatarColor;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastChatTime() {
        return lastChatTime;
    }

    public void setLastChatTime(long lastChatTime) {
        this.lastChatTime = lastChatTime;
    }
}
