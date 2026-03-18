# EMSaide - 邮件聊天 Android 应用

## 项目概述

EMSaide 是一个创新的 Android 应用，将电子邮件服务转换为类似聊天的体验。用户可以通过 POP3/SMTP/IMAP 协议管理多个邮箱账户，以聊天的形式发送和接收邮件。

## 核心功能

### 1. 多邮箱账户管理
- 支持添加多个邮箱账户（Gmail、QQ 邮箱、网易邮箱、Outlook 等）
- 每个邮箱账户作为一个独立的聊天对象
- 支持自定义账户名称便于识别

### 2. 邮件服务器配置
- 支持 POP3 和 IMAP 协议接收邮件
- 支持 SMTP协议发送邮件
- 支持 SSL/TLS 加密连接
- 提供常用邮箱服务商的快速配置

### 3. 邮件处理
- 读取邮件后自动从服务器删除
- 所有聊天内容保存在本地 SQLite 数据库
- 支持手动同步新邮件

### 4. 聊天界面
- 类似微信/QQ 的聊天界面
- 消息按时间顺序显示
- 区分发送和接收的消息（不同颜色和位置）
- 支持系统消息显示

## 技术架构

### 技术栈
- **语言**: Java
- **最低 SDK**: API 24 (Android 7.0)
- **目标 SDK**: API 36
- **架构模式**: MVVM

### 主要库
- **Room**: 本地 SQLite 数据库
- **Navigation**: 片段导航
- **Lifecycle & ViewModel**: 生命周期感知组件
- **JavaMail Android**: POP3/IMAP/SMTP协议实现
- **Material Components**: Material Design UI 组件

### 项目结构

```
app/src/main/java/com/example/emsaide/
├── data/
│   ├── model/          # 数据模型
│   │   ├── EmailAccount.java    # 邮箱账户实体
│   │   └── ChatMessage.java     # 聊天消息实体
│   ├── dao/            # 数据访问对象
│   │   ├── EmailAccountDao.java
│   │   └── ChatMessageDao.java
│   ├── database/       # Room 数据库
│   │   └── AppDatabase.java
│   └── repository/     # 数据仓库
│       └── EmailRepository.java
├── service/            # 邮件服务
│   └── EmailService.java
├── ui/                 # UI 组件
│   ├── ChatListFragment.java       # 聊天列表界面
│   ├── ChatDetailFragment.java     # 聊天详情界面
│   ├── EmailSettingsFragment.java  # 邮箱设置界面
│   ├── ChatListAdapter.java        # 聊天列表适配器
│   ├── MessageListAdapter.java     # 消息列表适配器
│   ├── ChatListViewModel.java      # 聊天列表 ViewModel
│   └── ChatDetailViewModel.java    # 聊天详情 ViewModel
└── MainActivity.java   # 主 Activity
```

## 使用说明

### 1. 添加邮箱账户

1. 点击主页右下角的 "+" 按钮
2. 选择邮箱服务商（或手动配置）
3. 填写邮箱地址和密码
4. 配置服务器设置（快速配置会自动填充）
5. 点击"测试连接"验证配置
6. 点击"保存"添加账户

### 2. 常用邮箱服务器配置

#### Gmail
- IMAP: imap.gmail.com:993 (SSL)
- POP3: pop.gmail.com:995 (SSL)
- SMTP: smtp.gmail.com:465 (SSL)

#### QQ 邮箱
- IMAP: imap.qq.com:993 (SSL)
- POP3: pop.qq.com:995 (SSL)
- SMTP: smtp.qq.com:465 (SSL)
- 注意：需要使用授权码而非密码

#### 网易 163 邮箱
- IMAP: imap.163.com:993 (SSL)
- POP3: pop.163.com:995 (SSL)
- SMTP: smtp.163.com:465 (SSL)

#### Outlook/Hotmail
- IMAP: outlook.office365.com:993 (SSL)
- POP3: outlook.office365.com:995 (SSL)
- SMTP: smtp.office365.com:465 (SSL)

### 3. 聊天和同步

- 点击邮箱账户进入聊天界面
- 长按"发送"按钮可以同步新邮件
- 输入消息后点击"发送"按钮发送邮件

## 注意事项

1. **安全性**: 
   - 密码以明文存储在本地数据库（生产环境应加密）
   - 建议使用邮箱授权码而非真实密码

2. **邮件删除**:
   - 读取后的邮件会从服务器删除
   - 所有聊天记录保存在本地

3. **网络权限**:
   - 应用需要 INTERNET 权限来连接邮件服务器
   - 请确保设备已连接网络

## 构建和运行

### 前提条件
- Android Studio Hedgehog 或更高版本
- JDK 11 或更高版本
- Android SDK 36

### 构建步骤

1. 克隆或下载项目
2. 用 Android Studio 打开项目
3. 等待 Gradle 同步完成
4. 连接 Android 设备或启动模拟器
5. 点击 Run 按钮

### Gradle 配置

项目使用 Gradle 版本目录 (libs.versions.toml) 管理依赖：

```toml
[versions]
room = "2.6.1"
navigation = "2.7.7"
javamail = "1.6.7"
```

## 后续改进建议

1. **安全性增强**
   - 使用 Android Keystore 加密存储密码
   - 支持 OAuth2 认证

2. **功能增强**
   - 支持附件处理
   - 支持 HTML 邮件
   - 支持邮件搜索
   - 支持文件夹/标签管理

3. **用户体验**
   - 添加下拉刷新同步
   - 添加新邮件通知
   - 支持消息撤回
   - 支持草稿箱

4. **性能优化**
   - 实现后台同步服务
   - 添加图片缓存
   - 优化数据库查询

## 许可证

本项目仅供学习和研究使用。
