package com.example.emsaide.data.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.example.emsaide.data.model.Contact;

import java.util.List;

/**
 * 联系人数据访问对象
 */
@Dao
public interface ContactDao {
    
    @Insert
    long insert(Contact contact);
    
    @Update
    void update(Contact contact);
    
    @Delete
    void delete(Contact contact);
    
    @Query("SELECT * FROM contacts ORDER BY lastChatTime DESC")
    List<Contact> getAllContacts();
    
    @Query("SELECT * FROM contacts ORDER BY lastChatTime DESC")
    LiveData<List<Contact>> getAllContactsLiveData();
    
    @Query("SELECT * FROM contacts WHERE id = :id")
    Contact getContactById(long id);
    
    @Query("SELECT * FROM contacts WHERE email = :email LIMIT 1")
    Contact getContactByEmail(String email);
    
    @Query("DELETE FROM contacts WHERE id = :id")
    void deleteById(long id);
}
