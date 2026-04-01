package com.example.emsaide.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.emsaide.data.model.Conversation;

import java.util.List;

/**
 * 对话数据访问对象
 */
@Dao
public interface ConversationDao {
    
    @Insert
    long insert(Conversation conversation);
    
    @Update
    void update(Conversation conversation);
    
    @Delete
    void delete(Conversation conversation);
    
    @Query("SELECT * FROM conversations WHERE contactId = :contactId AND accountId = :accountId LIMIT 1")
    Conversation getConversation(long contactId, long accountId);
    
    @Query("SELECT * FROM conversations WHERE contactId = :contactId ORDER BY createdAt DESC LIMIT 1")
    Conversation getLatestConversation(long contactId);
    
    @Query("UPDATE conversations SET unreadCount = unreadCount + 1 WHERE id = :id")
    void incrementUnreadCount(long id);
    
    @Query("UPDATE conversations SET unreadCount = 0 WHERE id = :id")
    void clearUnreadCount(long id);
}
