package com.example.emsaide.service;

import android.util.Log;

import com.example.emsaide.data.model.EmailAccount;
import com.sun.mail.pop3.POP3Store;
import com.sun.mail.imap.IMAPStore;
import com.sun.mail.smtp.SMTPTransport;

import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.FetchProfile;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Store;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * 邮件服务类
 * 处理 POP3/IMAP/SMTP协议相关操作
 */
public class EmailService {
    
    private static final String TAG = "EmailService";
    
    private final EmailAccount account;
    private Session session;
    
    public EmailService(EmailAccount account) {
        this.account = account;
        initSession();
    }
    
    /**
     * 初始化邮件会话
     */
    private void initSession() {
        Properties props = new Properties();
        
        // SMTP 配置
        props.put("mail.smtp.host", account.getSmtpServer());
        props.put("mail.smtp.port", String.valueOf(account.getSmtpPort()));
        
        if (account.isUseSsl()) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.ssl.trust", account.getSmtpServer());
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        }
        
        props.put("mail.smtp.auth", "true");
        
        // POP3 配置
        props.put("mail.pop3.host", account.getPop3Server());
        props.put("mail.pop3.port", String.valueOf(account.getPop3Port()));
        if (account.isUseSsl()) {
            props.put("mail.pop3.ssl.enable", "true");
            props.put("mail.pop3.ssl.trust", account.getPop3Server());
        }
        
        // IMAP 配置
        props.put("mail.imap.host", account.getImapServer());
        props.put("mail.imap.port", String.valueOf(account.getImapPort()));
        if (account.isUseSsl()) {
            props.put("mail.imap.ssl.enable", "true");
            props.put("mail.imap.ssl.trust", account.getImapServer());
        }
        
        session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(account.getEmail(), account.getPassword());
            }
        });
    }
    
    /**
     * 发送 email
     * @param to 收件人
     * @param subject 主题
     * @param content 内容
     */
    public void sendEmail(String to, String subject, String content) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(account.getEmail()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(content);
        
        Transport.send(message);
    }
    
    /**
     * 使用 SMTP 发送邮件
     */
    public void sendEmailSmtp(String to, String subject, String content) throws MessagingException {
        MimeMessage message = new MimeMessage(session);
        message.setFrom(new InternetAddress(account.getEmail()));
        message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
        message.setSubject(subject);
        message.setText(content);
        
        SMTPTransport t = (SMTPTransport) session.getTransport("smtp");
        try {
            t.connect();
            t.sendMessage(message, message.getAllRecipients());
        } finally {
            t.close();
        }
    }
    
    /**
     * 接收邮件（POP3 或 IMAP）
     * @return 邮件消息数组
     */
    public Message[] receiveEmails() throws MessagingException {
        Store store = null;
        Folder folder = null;
        
        try {
            if (account.isUseImap()) {
                // 使用 IMAP
                store = session.getStore("imap");
                store.connect(account.getImapServer(), account.getImapPort(), 
                             account.getEmail(), account.getPassword());
                
                folder = store.getFolder("INBOX");
                folder.open(Folder.READ_WRITE);
                
                // 获取所有邮件
                Message[] messages = folder.getMessages();
                
                // 按日期排序，最新的在前
                // 注意：这里不删除邮件，删除逻辑在调用处处理
                
                return messages;
                
            } else {
                // 使用 POP3
                store = session.getStore("pop3");
                store.connect(account.getPop3Server(), account.getPop3Port(),
                             account.getEmail(), account.getPassword());
                
                folder = store.getFolder("INBOX");
                folder.open(Folder.READ_WRITE);
                
                Message[] messages = folder.getMessages();
                
                return messages;
            }
        } finally {
            if (folder != null && folder.isOpen()) {
                folder.close(false); // false 表示不删除邮件
            }
            if (store != null && store.isConnected()) {
                store.close();
            }
        }
    }
    
    /**
     * 接收邮件并删除（POP3）
     * @return 邮件消息数组
     */
    public Message[] receiveAndDeleteEmails() throws MessagingException {
        Store store = null;
        Folder folder = null;
        
        try {
            if (account.isUseImap()) {
                // IMAP 模式下，移动到已删除文件夹
                store = session.getStore("imap");
                store.connect(account.getImapServer(), account.getImapPort(),
                        account.getEmail(), account.getPassword());
                
                folder = store.getFolder("INBOX");
                folder.open(Folder.READ_WRITE);
                
                Message[] messages = folder.getMessages();
                
                // 标记为删除
                for (Message msg : messages) {
                    msg.setFlag(javax.mail.Flags.Flag.DELETED, true);
                }
                
                folder.expunge(); // 真正删除
                
                return messages;
                
            } else {
                // POP3 模式
                store = session.getStore("pop3");
                store.connect(account.getPop3Server(), account.getPop3Port(),
                        account.getEmail(), account.getPassword());
                
                folder = store.getFolder("INBOX");
                folder.open(Folder.READ_WRITE);
                
                Message[] messages = folder.getMessages();
                
                // 标记为删除
                for (Message msg : messages) {
                    msg.setFlag(javax.mail.Flags.Flag.DELETED, true);
                }
                
                return messages;
                
            }
        } finally {
            if (folder != null && folder.isOpen()) {
                folder.close(true); // true 表示删除已标记的邮件
            }
            if (store != null && store.isConnected()) {
                store.close();
            }
        }
    }
    
    /**
     * 测试连接
     * @return 是否成功
     */
    public boolean testConnection() {
        try {
            if (account.isUseImap()) {
                Store store = null;
                try {
                    store = session.getStore("imap");
                    store.connect(account.getImapServer(), account.getImapPort(),
                            account.getEmail(), account.getPassword());
                    return store.isConnected();
                } finally {
                    if (store != null && store.isConnected()) {
                        store.close();
                    }
                }
            } else {
                Store store = null;
                try {
                    store = session.getStore("pop3");
                    store.connect(account.getPop3Server(), account.getPop3Port(),
                            account.getEmail(), account.getPassword());
                    return store.isConnected();
                } finally {
                    if (store != null && store.isConnected()) {
                        store.close();
                    }
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Connection test failed", e);
            return false;
        }
    }
    
    /**
     * 获取会话对象
     */
    public Session getSession() {
        return session;
    }
}
