package com.example.emsaide.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.emsaide.data.model.EmailAccount;

import java.util.List;

/**
 * 邮箱账户数据访问对象
 */
@Dao
public interface EmailAccountDao {
    
    @Insert
    long insert(EmailAccount account);
    
    @Update
    void update(EmailAccount account);
    
    @Delete
    void delete(EmailAccount account);
    
    @Query("SELECT * FROM email_accounts ORDER BY createdAt DESC")
    List<EmailAccount> getAllAccounts();
    
    @Query("SELECT * FROM email_accounts ORDER BY createdAt DESC")
    LiveData<List<EmailAccount>> getAllAccountsLiveData();
    
    @Query("SELECT * FROM email_accounts WHERE id = :id")
    EmailAccount getAccountById(long id);
    
    @Query("SELECT * FROM email_accounts WHERE id = :id")
    LiveData<EmailAccount> getAccountByIdLiveData(long id);
    
    @Query("SELECT * FROM email_accounts WHERE email = :email LIMIT 1")
    EmailAccount getAccountByEmail(String email);
    
    @Query("DELETE FROM email_accounts WHERE id = :id")
    void deleteById(long id);
    
    @Query("UPDATE email_accounts SET lastSyncTime = :timestamp WHERE id = :id")
    void updateLastSyncTime(long id, long timestamp);
}
