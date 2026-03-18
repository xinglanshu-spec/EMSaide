package com.example.emsaide.data.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * 邮箱账户实体类
 * 存储每个邮箱的配置信息
 */
@Entity(tableName = "email_accounts")
public class EmailAccount {
    
    @PrimaryKey(autoGenerate = true)
    private long id;
    
    /** 邮箱地址 */
    private String email;
    
    /** 密码（实际应用中应该加密存储） */
    private String password;
    
    /** POP3 服务器地址 */
    private String pop3Server;
    
    /** POP3 服务器端口 */
    private int pop3Port;
    
    /** SMTP 服务器地址 */
    private String smtpServer;
    
    /** SMTP 服务器端口 */
    private int smtpPort;
    
    /** IMAP服务器地址（可选，如果使用 IMAP） */
    private String imapServer;
    
    /** IMAP服务器端口 */
    private int imapPort;
    
    /** 是否使用 SSL/TLS */
    private boolean useSsl;
    
    /** 是否使用 IMAP（否则使用 POP3） */
    private boolean useImap;
    
    /** 账户名称（用于聊天显示） */
    private String accountName;
    
    /** 创建时间 */
    private long createdAt;
    
    /** 最后同步时间 */
    private long lastSyncTime;

    public EmailAccount() {
        this.createdAt = System.currentTimeMillis();
    }

    // Getters and Setters
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getPop3Server() {
        return pop3Server;
    }

    public void setPop3Server(String pop3Server) {
        this.pop3Server = pop3Server;
    }

    public int getPop3Port() {
        return pop3Port;
    }

    public void setPop3Port(int pop3Port) {
        this.pop3Port = pop3Port;
    }

    public String getSmtpServer() {
        return smtpServer;
    }

    public void setSmtpServer(String smtpServer) {
        this.smtpServer = smtpServer;
    }

    public int getSmtpPort() {
        return smtpPort;
    }

    public void setSmtpPort(int smtpPort) {
        this.smtpPort = smtpPort;
    }

    public String getImapServer() {
        return imapServer;
    }

    public void setImapServer(String imapServer) {
        this.imapServer = imapServer;
    }

    public int getImapPort() {
        return imapPort;
    }

    public void setImapPort(int imapPort) {
        this.imapPort = imapPort;
    }

    public boolean isUseSsl() {
        return useSsl;
    }

    public void setUseSsl(boolean useSsl) {
        this.useSsl = useSsl;
    }

    public boolean isUseImap() {
        return useImap;
    }

    public void setUseImap(boolean useImap) {
        this.useImap = useImap;
    }

    public String getAccountName() {
        return accountName;
    }

    public void setAccountName(String accountName) {
        this.accountName = accountName;
    }

    public long getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(long createdAt) {
        this.createdAt = createdAt;
    }

    public long getLastSyncTime() {
        return lastSyncTime;
    }

    public void setLastSyncTime(long lastSyncTime) {
        this.lastSyncTime = lastSyncTime;
    }
}
