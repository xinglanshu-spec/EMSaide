package com.example.emsaide.data.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.emsaide.data.dao.ChatMessageDao;
import com.example.emsaide.data.dao.EmailAccountDao;
import com.example.emsaide.data.model.ChatMessage;
import com.example.emsaide.data.model.EmailAccount;

/**
 * 应用数据库
 * 版本 1: 初始版本
 */
@Database(entities = {EmailAccount.class, ChatMessage.class}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {
    
    private static volatile AppDatabase INSTANCE;
    private static final String DATABASE_NAME = "emsaide_db";
    
    public abstract EmailAccountDao emailAccountDao();
    
    public abstract ChatMessageDao chatMessageDao();
    
    /**
     * 获取单例数据库实例
     */
    public static AppDatabase getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            DATABASE_NAME
                    )
                    .fallbackToDestructiveMigration()
                    .build();
                }
            }
        }
        return INSTANCE;
    }
}
