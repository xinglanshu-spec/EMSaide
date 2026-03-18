package com.example.emsaide.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.emsaide.data.dao.ChatMessageDao;
import com.example.emsaide.data.dao.EmailAccountDao;
import com.example.emsaide.data.database.AppDatabase;
import com.example.emsaide.data.model.ChatMessage;
import com.example.emsaide.data.model.EmailAccount;
import com.example.emsaide.service.EmailService;
import com.sun.mail.pop3.POP3Folder;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.internet.MimeUtility;

/**
 * 数据仓库类
 * 统一管理数据操作
 */
public class EmailRepository {
    
    private static final String TAG = "EmailRepository";
    
    private final EmailAccountDao accountDao;
    private final ChatMessageDao messageDao;
    private final ExecutorService executor;
    
    public EmailRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        accountDao = database.emailAccountDao();
        messageDao = database.chatMessageDao();
        executor = Executors.newSingleThreadExecutor();
    }
    
    // ==================== 邮箱账户相关 ====================
    
    public long insertAccount(EmailAccount account) {
        return accountDao.insert(account);
    }
    
    public void updateAccount(EmailAccount account) {
        executor.execute(() -> accountDao.update(account));
    }
    
    public void deleteAccount(EmailAccount account) {
        executor.execute(() -> {
            // 同时删除关联的聊天消息
            messageDao.deleteAllForAccount(account.getId());
            accountDao.delete(account);
        });
    }
    
    public LiveData<List<EmailAccount>> getAllAccountsLiveData() {
        return accountDao.getAllAccountsLiveData();
    }
    
    public List<EmailAccount> getAllAccounts() {
        return accountDao.getAllAccounts();
    }
    
    public EmailAccount getAccountById(long id) {
        return accountDao.getAccountById(id);
    }
    
    public LiveData<EmailAccount> getAccountByIdLiveData(long id) {
        return accountDao.getAccountByIdLiveData(id);
    }
    
    // ==================== 聊天消息相关 ====================
    
    public long insertMessage(ChatMessage message) {
        return messageDao.insert(message);
    }
    
    public void insertMessages(List<ChatMessage> messages) {
        executor.execute(() -> messageDao.insertAll(messages));
    }
    
    public LiveData<List<ChatMessage>> getMessagesByAccountIdLiveData(long accountId) {
        return messageDao.getMessagesByAccountIdLiveData(accountId);
    }
    
    public List<ChatMessage> getMessagesByAccountId(long accountId) {
        return messageDao.getMessagesByAccountId(accountId);
    }
    
    public void markAllAsRead(long accountId) {
        executor.execute(() -> messageDao.markAllAsRead(accountId));
    }
    
    public int getUnreadCount(long accountId) {
        return messageDao.getUnreadCount(accountId);
    }
    
    // ==================== 邮件同步相关 ====================
    
    /**
     * 同步邮件并保存到本地
     * @param account 邮箱账户
     * @param callback 回调接口
     */
    public void syncEmails(EmailAccount account, SyncCallback callback) {
        executor.execute(() -> {
            try {
                EmailService emailService = new EmailService(account);
                
                // 接收邮件并删除
                Message[] messages = emailService.receiveAndDeleteEmails();
                
                if (messages != null && messages.length > 0) {
                    List<ChatMessage> chatMessages = new ArrayList<>();
                    
                    for (Message msg : messages) {
                        try {
                            ChatMessage chatMessage = new ChatMessage();
                            chatMessage.setAccountId(account.getId());
                            chatMessage.setType(ChatMessage.MessageType.RECEIVED);
                            
                            // 获取发件人
                            String from = "";
                            if (msg.getFrom() != null && msg.getFrom().length > 0) {
                                from = msg.getFrom()[0].toString();
                            }
                            chatMessage.setSender(from);
                            chatMessage.setReceiver(account.getEmail());
                            
                            // 获取主题
                            String subject = "";
                            if (msg.getSubject() != null) {
                                subject = MimeUtility.decodeText(msg.getSubject());
                            }
                            chatMessage.setSubject(subject);
                            
                            // 获取内容
                            String content = getEmailContent(msg);
                            chatMessage.setContent(content);
                            
                            // 获取时间
                            Date sentDate = msg.getSentDate();
                            chatMessage.setTimestamp(sentDate != null ? sentDate.getTime() : System.currentTimeMillis());
                            
                            // 获取邮件 ID
                            try {
                                String messageId = msg.getHeader("Message-ID") != null ? 
                                    msg.getHeader("Message-ID")[0] : "";
                                chatMessage.setEmailMessageId(messageId);
                            } catch (Exception e) {
                                Log.e(TAG, "Get message id error", e);
                            }
                            
                            chatMessage.setRead(false);
                            chatMessages.add(chatMessage);
                            
                        } catch (Exception e) {
                            Log.e(TAG, "Parse message error", e);
                        }
                    }
                    
                    // 批量插入到数据库
                    if (!chatMessages.isEmpty()) {
                        messageDao.insertAll(chatMessages);
                    }
                    
                    // 更新最后同步时间
                    accountDao.updateLastSyncTime(account.getId(), System.currentTimeMillis());
                    
                    if (callback != null) {
                        callback.onSuccess(chatMessages.size());
                    }
                } else {
                    if (callback != null) {
                        callback.onSuccess(0);
                    }
                }
                
            } catch (MessagingException e) {
                Log.e(TAG, "Sync emails error", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 获取邮件内容
     */
    private String getEmailContent(Message msg) throws Exception {
        Object content = msg.getContent();
        
        if (content == null) {
            return "";
        }
        
        if (content instanceof String) {
            return (String) content;
        }
        
        // 如果是 multipart 类型（包含附件等）
        if (content instanceof javax.mail.Multipart) {
            javax.mail.Multipart multipart = (javax.mail.Multipart) content;
            StringBuilder sb = new StringBuilder();
            
            for (int i = 0; i < multipart.getCount(); i++) {
                javax.mail.BodyPart bodyPart = multipart.getBodyPart(i);
                String disposition = bodyPart.getDisposition();
                
                // 只处理内联文本部分，跳过附件
                if (disposition == null || !disposition.equalsIgnoreCase(javax.mail.Part.ATTACHMENT)) {
                    Object partContent = bodyPart.getContent();
                    if (partContent instanceof String) {
                        sb.append(partContent.toString());
                    }
                }
            }
            
            return sb.toString();
        }
        
        return content.toString();
    }
    
    /**
     * 发送邮件
     */
    public void sendEmail(EmailAccount account, String to, String subject, String content, 
                         SendCallback callback) {
        executor.execute(() -> {
            try {
                EmailService emailService = new EmailService(account);
                emailService.sendEmailSmtp(to, subject, content);
                
                // 保存发送记录到数据库
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setAccountId(account.getId());
                chatMessage.setType(ChatMessage.MessageType.SENT);
                chatMessage.setSender(account.getEmail());
                chatMessage.setReceiver(to);
                chatMessage.setSubject(subject);
                chatMessage.setContent(content);
                chatMessage.setTimestamp(System.currentTimeMillis());
                chatMessage.setRead(true);
                
                messageDao.insert(chatMessage);
                
                if (callback != null) {
                    callback.onSuccess();
                }
                
            } catch (MessagingException e) {
                Log.e(TAG, "Send email error", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    /**
     * 测试邮箱连接
     */
    public boolean testConnection(EmailAccount account) {
        try {
            EmailService emailService = new EmailService(account);
            return emailService.testConnection();
        } catch (Exception e) {
            Log.e(TAG, "Test connection error", e);
            return false;
        }
    }
    
    /**
     * 同步回调接口
     */
    public interface SyncCallback {
        void onSuccess(int count);
        void onError(String error);
    }
    
    /**
     * 发送回调接口
     */
    public interface SendCallback {
        void onSuccess();
        void onError(String error);
    }
}
