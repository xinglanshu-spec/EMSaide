package com.example.emsaide.ui;

import android.app.Application;
import android.util.Log;

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
    
    private static final String TAG = "ChatDetailViewModel";
    
    private final EmailRepository repository;
    private final MutableLiveData<List<ChatMessage>> messages = new MutableLiveData<>();
    private LiveData<List<ChatMessage>> liveMessages;
    private EmailAccount account;
    private long accountId;
    
    public ChatDetailViewModel(@NonNull Application application) {
        super(application);
        repository = new EmailRepository(application);
    }
    
    private long contactId = -1;
    
    /**
     * 为联系人加载消息（使用第一个邮箱账户）
     */
    public void loadMessagesForContact(long contactId) {
        this.contactId = contactId;
        // 在后台线程获取邮箱账户
        new Thread(() -> {
            List<EmailAccount> accounts = repository.getAllAccounts();
            Log.d(TAG, "loadMessagesForContact: accounts count = " + (accounts != null ? accounts.size() : "null"));
            
            if (accounts != null && !accounts.isEmpty()) {
                account = accounts.get(0);
                accountId = account.getId();
                Log.d(TAG, "loadMessagesForContact: loaded account = " + account.getEmail());
                
                // 在主线程设置 LiveData 观察
                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                    liveMessages = repository.getMessagesByConversationIdLiveData(contactId);
                    liveMessages.observeForever(msgs -> {
                        if (msgs != null) {
                            messages.setValue(msgs);
                        }
                    });
                });
            } else {
                Log.e(TAG, "loadMessagesForContact: no email accounts found in database");
            }
        }).start();
    }
    
    /**
     * 加载消息（旧方法，保持兼容）
     */
    @Deprecated
    public void loadMessages(long accountId) {
        this.accountId = accountId;
        this.account = repository.getAccountById(accountId);
        
        if (account != null) {
            // TODO: 需要实现按对话加载
            // liveMessages = repository.getMessagesByAccountIdLiveData(accountId);
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
    public void sendEmail(String to, String subject, String content, SendCallback callback) {
        Log.d(TAG, "sendEmail: account is null? " + (account == null));
        
        // 如果账户未加载，在后台线程获取
        if (account == null) {
            new Thread(() -> {
                try {
                    List<EmailAccount> accounts = repository.getAllAccounts();
                    Log.d(TAG, "sendEmail: fetched accounts count = " + (accounts != null ? accounts.size() : "null"));
                    
                    if (accounts != null && !accounts.isEmpty()) {
                        account = accounts.get(0);
                        accountId = account.getId();
                        Log.d(TAG, "sendEmail: loaded account = " + account.getEmail());
                        
                        // 账户获取成功，继续发送邮件
                        sendEmailInternal(to, subject, content, callback);
                    } else {
                        Log.e(TAG, "sendEmail: no email accounts in database");
                        postError(callback, "请先配置邮箱账户");
                    }
                } catch (Exception e) {
                    Log.e(TAG, "sendEmail: error getting accounts", e);
                    postError(callback, "无法获取邮箱账户");
                }
            }).start();
        } else {
            // 账户已加载，直接发送
            sendEmailInternal(to, subject, content, callback);
        }
    }
    
    /**
     * 内部方法：执行实际的邮件发送
     */
    private void sendEmailInternal(String to, String subject, String content, SendCallback callback) {
        if (to == null || to.isEmpty()) {
            callback.onError("请输入收件人邮箱");
            return;
        }
        
        if (subject == null || subject.isEmpty()) {
            subject = "聊天消息";
        }
        
        repository.sendEmail(account, to, subject, content, contactId, 
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
     * 在主线程报告错误
     */
    private void postError(SendCallback callback, String errorMessage) {
        new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
            callback.onError(errorMessage);
        });
    }
    
    /**
     * 标记所有消息为已读
     */
    public void markAllAsRead() {
        repository.markAllAsRead(contactId);
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
