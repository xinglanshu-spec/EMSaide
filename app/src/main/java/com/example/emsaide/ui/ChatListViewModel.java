package com.example.emsaide.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.emsaide.data.model.EmailAccount;
import com.example.emsaide.data.repository.EmailRepository;

import java.util.List;

/**
 * 聊天列表 ViewModel
 */
public class ChatListViewModel extends AndroidViewModel {
    
    private final EmailRepository repository;
    private final LiveData<List<EmailAccount>> accounts;
    
    public ChatListViewModel(@NonNull Application application) {
        super(application);
        repository = new EmailRepository(application);
        accounts = repository.getAllAccountsLiveData();
    }
    
    public LiveData<List<EmailAccount>> getAccounts() {
        return accounts;
    }
}
