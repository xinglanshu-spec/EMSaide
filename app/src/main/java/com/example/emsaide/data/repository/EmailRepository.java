package com.example.emsaide.data.repository;

import android.content.Context;
import android.util.Log;

import androidx.lifecycle.LiveData;

import com.example.emsaide.data.dao.ChatMessageDao;
import com.example.emsaide.data.dao.ContactDao;
import com.example.emsaide.data.dao.EmailAccountDao;
import com.example.emsaide.data.database.AppDatabase;
import com.example.emsaide.data.model.ChatMessage;
import com.example.emsaide.data.model.Contact;
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
    private final ContactDao contactDao;
    private final ChatMessageDao messageDao;
    private final ExecutorService executor;
    
    public EmailRepository(Context context) {
        AppDatabase database = AppDatabase.getInstance(context);
        accountDao = database.emailAccountDao();
        contactDao = database.contactDao();
        messageDao = database.chatMessageDao();
        executor = Executors.newSingleThreadExecutor();
    }
    
    // ==================== 邮箱账户相关 ====================
    
    public long insertAccount(EmailAccount account) {
        long id = accountDao.insert(account);
        Log.d(TAG, "insertAccount: id = " + id + ", email = " + account.getEmail());
        return id;
    }
    
    public void insertAccountAsync(EmailAccount account, InsertCallback callback) {
        executor.execute(() -> {
            try {
                long id = accountDao.insert(account);
                if (callback != null) {
                    callback.onSuccess(id);
                }
            } catch (Exception e) {
                Log.e(TAG, "Insert account error", e);
                if (callback != null) {
                    callback.onError(e.getMessage());
                }
            }
        });
    }
    
    public interface InsertCallback {
        void onSuccess(long id);
        void onError(String error);
    }
    
    public void updateAccount(EmailAccount account) {
        executor.execute(() -> accountDao.update(account));
    }
    
    public void deleteAccount(EmailAccount account) {
        // TODO: 需要更新为按对话删除
        // 暂时保留，等待后续完善
        accountDao.delete(account);
    }
    
    public List<EmailAccount> getAllAccounts() {
        List<EmailAccount> accounts = accountDao.getAllAccounts();
        Log.d(TAG, "getAllAccounts: count = " + (accounts != null ? accounts.size() : "null"));
        return accounts;
    }
    
    public LiveData<List<EmailAccount>> getAllAccountsLiveData() {
        return accountDao.getAllAccountsLiveData();
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
    
    public LiveData<List<ChatMessage>> getMessagesByConversationIdLiveData(long conversationId) {
        return messageDao.getMessagesByConversationIdLiveData(conversationId);
    }
    
    public List<ChatMessage> getMessagesByAccountId(long accountId) {
        return messageDao.getMessagesByConversationId(accountId);
    }
    
    public void markAllAsRead(long conversationId) {
        executor.execute(() -> messageDao.markAllAsRead(conversationId));
    }
    
    public int getUnreadCount(long conversationId) {
        return messageDao.getUnreadCount(conversationId);
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
                Log.d(TAG, "syncEmails: start syncing for account " + account.getEmail());
                
                EmailService emailService = new EmailService(account);
                
                // 接收邮件（不删除，增量同步）
                Message[] messages = emailService.receiveAllEmails();
                
                Log.d(TAG, "syncEmails: received " + (messages != null ? messages.length : 0) + " messages");
                
                if (messages != null && messages.length > 0) {
                    List<ChatMessage> chatMessages = new ArrayList<>();
                    
                    for (Message msg : messages) {
                        try {
                            // 获取邮件 ID，用于去重
                            String emailMessageId = "";
                            try {
                                String[] headers = msg.getHeader("Message-ID");
                                emailMessageId = (headers != null && headers.length > 0) ? headers[0] : "";
                            } catch (Exception e) {
                                Log.e(TAG, "Get message id error", e);
                            }
                            
                            // 跳过已同步的邮件
                            if (!emailMessageId.isEmpty() && messageDao.existsByEmailMessageId(emailMessageId) > 0) {
                                Log.d(TAG, "syncEmails: skipping duplicate message " + emailMessageId);
                                continue;
                            }
                            
                            ChatMessage chatMessage = new ChatMessage();
                            chatMessage.setAccountId(account.getId());
                            chatMessage.setType(ChatMessage.MessageType.RECEIVED);
                            chatMessage.setEmailMessageId(emailMessageId);
                            
                            // 获取发件人
                            String from = "";
                            if (msg.getFrom() != null && msg.getFrom().length > 0) {
                                from = msg.getFrom()[0].toString();
                            }
                            chatMessage.setSender(from);
                            chatMessage.setReceiver(account.getEmail());
                            
                            // 根据发件人邮箱查找联系人，设置 conversationId
                            String senderEmail = extractEmailFromAddress(from);
                            Log.d(TAG, "syncEmails: senderEmail = " + senderEmail);
                            
                            if (senderEmail != null) {
                                senderEmail = senderEmail.trim().toLowerCase();
                                
                                // 首先尝试精确匹配
                                Contact contact = contactDao.getContactByEmail(senderEmail);
                                
                                // 精确匹配失败，尝试模糊匹配
                                if (contact == null) {
                                    List<Contact> allContacts = contactDao.getAllContacts();
                                    if (allContacts != null) {
                                        for (Contact c : allContacts) {
                                            if (c.getEmail() != null && c.getEmail().trim().toLowerCase().equals(senderEmail)) {
                                                contact = c;
                                                break;
                                            }
                                        }
                                    }
                                }
                                
                                if (contact != null) {
                                    chatMessage.setConversationId(contact.getId());
                                    // 更新联系人的最后聊天时间
                                    contact.setLastChatTime(System.currentTimeMillis());
                                    contactDao.update(contact);
                                } else {
                                    // 创建新联系人
                                    Contact newContact = new Contact();
                                    newContact.setEmail(senderEmail);
                                    String senderName = from;
                                    if (from.contains("<")) {
                                        senderName = from.substring(0, from.indexOf("<")).trim();
                                    }
                                    newContact.setName(senderName.isEmpty() ? senderEmail : senderName);
                                    newContact.setLastChatTime(System.currentTimeMillis());
                                    long contactId = contactDao.insert(newContact);
                                    chatMessage.setConversationId(contactId);
                                    Log.d(TAG, "syncEmails: created new contact with id = " + contactId);
                                }
                            }
                            
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
                            
                            chatMessage.setRead(false);
                            chatMessages.add(chatMessage);
                            
                        } catch (Exception e) {
                            Log.e(TAG, "Parse message error", e);
                        }
                    }
                    
                    // 批量插入到数据库
                    if (!chatMessages.isEmpty()) {
                        List<Long> ids = messageDao.insertAll(chatMessages);
                        Log.d(TAG, "syncEmails: inserted " + ids.size() + " messages to database");
                        
                        // 打印插入的消息详情
                        for (int i = 0; i < chatMessages.size(); i++) {
                            ChatMessage cm = chatMessages.get(i);
                            Log.d(TAG, "syncEmails: message[" + i + "] conversationId=" + cm.getConversationId() + 
                                ", type=" + cm.getType() + ", sender=" + cm.getSender());
                        }
                    }
                    
                    // 更新最后同步时间
                    accountDao.updateLastSyncTime(account.getId(), System.currentTimeMillis());
                    
                    Log.d(TAG, "syncEmails: sync completed successfully");
                    
                    if (callback != null) {
                        callback.onSuccess(chatMessages.size());
                    }
                } else {
                    Log.d(TAG, "syncEmails: no new messages");
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
     * 从发件人字符串中提取邮箱地址
     */
    private String extractEmailFromAddress(String address) {
        if (address == null || address.isEmpty()) {
            return null;
        }
        
        // 匹配邮箱地址的正则表达式
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}");
        java.util.regex.Matcher matcher = pattern.matcher(address);
        
        if (matcher.find()) {
            return matcher.group();
        }
        
        return null;
    }
    
    /**
     * 获取邮件内容（递归处理 multipart，优先获取文本部分）
     */
    private String getEmailContent(Message msg) throws Exception {
        Object content = msg.getContent();
        return extractTextPart(content);
    }
    
    /**
     * 从邮件内容中提取文本，优先返回 text/plain
     */
    private String extractTextPart(Object content) throws Exception {
        if (content == null) {
            return "";
        }
        
        if (content instanceof String) {
            return (String) content;
        }
        
        if (content instanceof javax.mail.Multipart) {
            javax.mail.Multipart multipart = (javax.mail.Multipart) content;
            String htmlContent = null;
            StringBuilder textContent = new StringBuilder();
            
            for (int i = 0; i < multipart.getCount(); i++) {
                javax.mail.BodyPart bodyPart = multipart.getBodyPart(i);
                String disposition = bodyPart.getDisposition();
                
                // 跳过附件
                if (disposition != null && disposition.equalsIgnoreCase(javax.mail.Part.ATTACHMENT)) {
                    continue;
                }
                
                String contentType = bodyPart.getContentType();
                if (contentType != null && contentType.toLowerCase().contains("multipart")) {
                    // 递归处理嵌套 multipart
                    String nested = extractTextPart(bodyPart.getContent());
                    if (!nested.isEmpty()) {
                        if (contentType.toLowerCase().contains("text/html")) {
                            htmlContent = nested;
                        } else {
                            textContent.append(nested);
                        }
                    }
                } else if (contentType != null && contentType.toLowerCase().contains("text/html")) {
                    // 先缓存 HTML，优先用纯文本
                    Object partContent = bodyPart.getContent();
                    if (partContent instanceof String) {
                        htmlContent = (String) partContent;
                    }
                } else {
                    // 纯文本
                    Object partContent = bodyPart.getContent();
                    if (partContent instanceof String) {
                        textContent.append(partContent.toString());
                    }
                }
            }
            
            // 优先返回纯文本，没有纯文本则返回 HTML
            if (textContent.length() > 0) {
                return textContent.toString();
            }
            if (htmlContent != null) {
                return htmlContent;
            }
        }
        
        return content.toString();
    }
    
    /**
     * 发送邮件
     */
    public void sendEmail(EmailAccount account, String to, String subject, String content, long contactId, 
                         SendCallback callback) {
        executor.execute(() -> {
            try {
                EmailService emailService = new EmailService(account);
                emailService.sendEmailSmtp(to, subject, content);
                
                // 保存发送记录到数据库
                ChatMessage chatMessage = new ChatMessage();
                chatMessage.setAccountId(account.getId());
                chatMessage.setConversationId(contactId);
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
