package com.example.emsaide.service;

import com.example.emsaide.data.model.EmailAccount;

/**
 * 邮箱配置工具类
 * 提供常用邮箱服务商的默认配置
 */
public class EmailConfigUtil {
    
    /**
     * 邮箱服务商配置
     */
    public static class ProviderConfig {
        public String name;
        public String pop3Server;
        public int pop3PortSsl;
        public int pop3PortNoSsl;
        public String imapServer;
        public int imapPortSsl;
        public int imapPortNoSsl;
        public String smtpServer;
        public int smtpPortSsl;
        public int smtpPortNoSsl;
        
        public ProviderConfig(String name, String pop3Server, int pop3PortSsl, int pop3PortNoSsl,
                             String imapServer, int imapPortSsl, int imapPortNoSsl,
                             String smtpServer, int smtpPortSsl, int smtpPortNoSsl) {
            this.name = name;
            this.pop3Server = pop3Server;
            this.pop3PortSsl = pop3PortSsl;
            this.pop3PortNoSsl = pop3PortNoSsl;
            this.imapServer = imapServer;
            this.imapPortSsl = imapPortSsl;
            this.imapPortNoSsl = imapPortNoSsl;
            this.smtpServer = smtpServer;
            this.smtpPortSsl = smtpPortSsl;
            this.smtpPortNoSsl = smtpPortNoSsl;
        }
    }
    
    // 常用邮箱服务商配置
    public static final ProviderConfig GMAIL = new ProviderConfig(
        "Gmail",
        "pop.gmail.com", 995, 110,
        "imap.gmail.com", 993, 143,
        "smtp.gmail.com", 465, 587
    );
    
    public static final ProviderConfig QQ_EMAIL = new ProviderConfig(
        "QQ 邮箱",
        "pop.qq.com", 995, 110,
        "imap.qq.com", 993, 143,
        "smtp.qq.com", 465, 587
    );
    
    public static final ProviderConfig NETEASE_163 = new ProviderConfig(
        "网易 163 邮箱",
        "pop.163.com", 995, 110,
        "imap.163.com", 993, 143,
        "smtp.163.com", 465, 587
    );
    
    public static final ProviderConfig NETEASE_126 = new ProviderConfig(
        "网易 126 邮箱",
        "pop.126.com", 995, 110,
        "imap.126.com", 993, 143,
        "smtp.126.com", 465, 587
    );
    
    public static final ProviderConfig OUTLOOK = new ProviderConfig(
        "Outlook/Hotmail",
        "outlook.office365.com", 995, 110,
        "outlook.office365.com", 993, 143,
        "smtp.office365.com", 465, 587
    );
    
    public static final ProviderConfig YAHOO = new ProviderConfig(
        "Yahoo",
        "pop.mail.yahoo.com", 995, 110,
        "imap.mail.yahoo.com", 993, 143,
        "smtp.mail.yahoo.com", 465, 587
    );
    
    /**
     * 根据名称获取配置
     */
    public static ProviderConfig getProviderByName(String name) {
        if (name == null) return null;
        
        switch (name) {
            case "Gmail":
                return GMAIL;
            case "QQ 邮箱":
                return QQ_EMAIL;
            case "网易 163 邮箱":
                return NETEASE_163;
            case "网易 126 邮箱":
                return NETEASE_126;
            case "Outlook/Hotmail":
                return OUTLOOK;
            case "Yahoo":
                return YAHOO;
            default:
                return null;
        }
    }
    
    /**
     * 填充账户配置
     */
    public static void fillAccountConfig(EmailAccount account, ProviderConfig config, boolean useSsl) {
        if (config == null || account == null) return;
        
        account.setPop3Server(config.pop3Server);
        account.setPop3Port(useSsl ? config.pop3PortSsl : config.pop3PortNoSsl);
        
        account.setImapServer(config.imapServer);
        account.setImapPort(useSsl ? config.imapPortSsl : config.imapPortNoSsl);
        
        account.setSmtpServer(config.smtpServer);
        account.setSmtpPort(useSsl ? config.smtpPortSsl : config.smtpPortNoSsl);
        
        account.setUseSsl(useSsl);
    }
}
