package com.example.emsaide.ui;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.emsaide.data.model.ChatMessage;
import com.example.emsaide.data.model.EmailAccount;
import com.example.emsaide.data.repository.EmailRepository;

import java.util.List;

/**
 * 聊天详情 ViewModel
 */
public class ChatDetailViewModel extends AndroidViewModel {
    
    private final EmailRepository repository;
    private final MutableLiveData<List<ChatMessage>> messages = new MutableLiveData<>();
    private LiveData<List<ChatMessage>> liveMessages;
    private EmailAccount account;
    private long accountId;
    
    public ChatDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new EmailRepository(application);
    }
    
    /**
     * 加载消息
     */
    public void loadMessages(long accountId) {
        this.accountId = accountId;
        this.account = repository.getAccountById(accountId);
        
        if (account != null) {
            liveMessages = repository.getMessagesByAccountIdLiveData(accountId);
            liveMessages.observeForever(msgs -> {
                if (msgs != null) {
                    messages.setValue(msgs);
                }
            });
        }
    }
    
    public LiveData<List<ChatMessage>> getMessages() {
        return messages;
    }
    
    /**
     * 同步邮件
     */
    public void syncEmails(SyncCallback callback) {
        if (account == null) {
            callback.onError("账户不存在");
            return;
        }
        
        repository.syncEmails(account, new EmailRepository.SyncCallback() {
            @Override
            public void onSuccess(int count) {
                callback.onSuccess(count);
            }
            
            @Override
            public void onError(String error) {
                callback.onError(error);
            }
        });
    }
    
    /**
     * 发送邮件
     */
    public void sendEmail(String content, SendCallback callback) {
        if (account == null) {
            callback.onError("账户不存在");
            return;
        }
        
        // 这里简化处理，发送给自己作为测试
        // 实际应用中应该解析收件人
        repository.sendEmail(account, account.getEmail(), "聊天消息", content, 
            new EmailRepository.SendCallback() {
                @Override
                public void onSuccess() {
                    callback.onSuccess();
                }
                
                @Override
                public void onError(String error) {
                    callback.onError(error);
                }
            });
    }
    
    /**
     * 标记所有消息为已读
     */
    public void markAllAsRead() {
        repository.markAllAsRead(accountId);
    }
    
    public interface SyncCallback {
        void onSuccess(int count);
        void onError(String error);
    }
    
    public interface SendCallback {
        void onSuccess();
        void onError(String error);
    }
}
