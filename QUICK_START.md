# EMSaide 快速启动指南

## 第一步：同步 Gradle 依赖

打开 Android Studio 后，等待 Gradle 自动同步完成。如果同步失败，请检查：

1. 网络连接是否正常
2. Gradle 版本是否兼容（需要 Gradle 8.x）
3. Android Gradle Plugin 版本（需要 8.13.0）

如果遇到 Maven Central 连接问题，可以配置国内镜像。

## 第二步：配置测试邮箱

### Gmail 配置
```
邮箱地址：your@gmail.com
密码：应用专用密码（需要在 Google 账户中生成）

IMAP: imap.gmail.com:993 (SSL)
SMTP: smtp.gmail.com:465 (SSL)
```

### QQ 邮箱配置
```
邮箱地址：your@qq.com
密码：授权码（在 QQ 邮箱设置中获取）

IMAP: imap.qq.com:993 (SSL)
SMTP: smtp.qq.com:465 (SSL)
```

### 网易 163 邮箱配置
```
邮箱地址：your@163.com
密码：授权码（在网易邮箱设置中获取）

IMAP: imap.163.com:993 (SSL)
SMTP: smtp.163.com:465 (SSL)
```

## 第三步：运行应用

1. 连接 Android 设备或启动模拟器（API 24+）
2. 点击 Run 按钮（绿色三角形）
3. 等待应用安装和启动

## 第四步：添加邮箱账户

1. 点击主页右下角的 "+" 按钮
2. 从下拉列表选择邮箱服务商
3. 填写邮箱地址和密码/授权码
4. 服务器设置会自动填充
5. 点击"测试连接"验证配置
6. 点击"保存"添加账户

## 第五步：同步和发送邮件

### 同步邮件
1. 点击邮箱账户进入聊天界面
2. 长按"发送"按钮
3. 确认同步对话框

### 发送邮件
1. 在输入框中输入消息
2. 点击"发送"按钮
3. 邮件会通过 SMTP 发送

## 常见问题解决

### 1. Gradle 同步失败

**错误**: Could not resolve all dependencies

**解决**:
- 检查网络连接
- 清理 Gradle 缓存：File -> Invalidate Caches / Restart
- 检查 gradle.properties 中的 JVM 参数

### 2. 无法连接邮箱服务器

**错误**: Connection timed out / Connect failed

**可能原因**:
- 网络连接问题
- 防火墙阻止
- 邮箱服务器地址或端口错误
- SSL 证书问题

**解决方法**:
- 检查网络连接
- 尝试切换 SSL/TLS选项
- 确认使用正确的服务器地址
- 某些邮箱需要特殊网络环境（如 Gmail）

### 3. 认证失败

**错误**: Authentication failed / Invalid credentials

**可能原因**:
- 密码错误
- 使用了登录密码而非授权码
- 邮箱开启了安全保护

**解决方法**:
- 确认使用授权码而非登录密码
- 在邮箱设置中重新生成授权码
- 检查是否开启了 POP3/IMAP服务

### 4. 应用崩溃

**错误**: App keeps crashing

**可能原因**:
- Room 数据库迁移问题
- 权限未正确配置

**解决方法**:
- 卸载应用后重新安装
- 清理应用数据
- 检查 Logcat 日志定位问题

## 开发调试技巧

### 查看日志
```bash
adb logcat | grep EmailService
adb logcat | grep EmailRepository
```

### 查看数据库
```bash
adb shell
run-as com.example.emsaide
cd databases
sqlite3 emsaide.db

# 查询邮箱账户
SELECT * FROM email_accounts;

# 查询聊天消息
SELECT * FROM chat_messages;
```

### 清除应用数据
```bash
adb shell pm clear com.example.emsaide
```

## 下一步

1. 熟悉应用的各个功能
2. 尝试添加不同的邮箱账户
3. 测试邮件同步和发送功能
4. 根据需求调整 UI 和功能
5. 考虑添加更多增强功能

祝你使用愉快！
