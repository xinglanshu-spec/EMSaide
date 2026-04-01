package com.example.emsaide.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.emsaide.data.model.ChatMessage;

import java.util.List;

/**
 * 聊天消息数据访问对象
 */
@Dao
public interface ChatMessageDao {
    
    @Insert
    long insert(ChatMessage message);
    
    @Insert
    List<Long> insertAll(List<ChatMessage> messages);
    
    @Update
    void update(ChatMessage message);
    
    @Delete
    void delete(ChatMessage message);
    
    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    List<ChatMessage> getMessagesByConversationId(long conversationId);
    
    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY timestamp ASC")
    LiveData<List<ChatMessage>> getMessagesByConversationIdLiveData(long conversationId);
    
    @Query("SELECT * FROM chat_messages WHERE id = :id")
    ChatMessage getMessageById(long id);
    
    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId AND isRead = 0 ORDER BY timestamp ASC")
    List<ChatMessage> getUnreadMessages(long conversationId);
    
    @Query("UPDATE chat_messages SET isRead = 1 WHERE conversationId = :conversationId")
    void markAllAsRead(long conversationId);
    
    @Query("UPDATE chat_messages SET isRead = 1 WHERE id = :id")
    void markAsRead(long id);
    
    @Query("DELETE FROM chat_messages WHERE conversationId = :conversationId")
    void deleteAllForConversation(long conversationId);
    
    @Query("DELETE FROM chat_messages WHERE id = :id")
    void deleteById(long id);
    
    @Query("SELECT COUNT(*) FROM chat_messages WHERE conversationId = :conversationId AND isRead = 0")
    int getUnreadCount(long conversationId);
    
    @Query("SELECT * FROM chat_messages WHERE conversationId = :conversationId ORDER BY timestamp DESC LIMIT 1")
    ChatMessage getLatestMessage(long conversationId);
}
