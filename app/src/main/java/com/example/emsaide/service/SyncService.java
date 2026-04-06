package com.example.emsaide.service;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.example.emsaide.R;
import com.example.emsaide.data.model.EmailAccount;
import com.example.emsaide.data.repository.EmailRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 邮件同步服务
 * 用于后台定时同步邮件
 */
public class SyncService extends Service {
    
    private static final String TAG = "SyncService";
    public static final String ACTION_SYNC = "com.example.emsaide.action.SYNC";
    public static final String ACTION_SYNC_ALL = "com.example.emsaide.action.SYNC_ALL";
    public static final String EXTRA_ACCOUNT_ID = "account_id";
    public static final String CHANNEL_ID = "email_sync_channel";
    
    private ExecutorService executor;
    private EmailRepository repository;
    
    @Override
    public void onCreate() {
        super.onCreate();
        executor = Executors.newSingleThreadExecutor();
        repository = new EmailRepository(this);
        createNotificationChannel();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            if (ACTION_SYNC.equals(intent.getAction())) {
                long accountId = intent.getLongExtra(EXTRA_ACCOUNT_ID, -1);
                if (accountId != -1) {
                    syncAccount(accountId);
                }
            } else if (ACTION_SYNC_ALL.equals(intent.getAction())) {
                syncAllAccounts();
            }
        }
        return START_NOT_STICKY;
    }
    
    /**
     * 同步单个账户
     */
    private void syncAccount(long accountId) {
        executor.execute(() -> {
            Log.d(TAG, "Start syncing account: " + accountId);
            
            try {
                EmailAccount account = repository.getAccountById(accountId);
                if (account == null) {
                    Log.w(TAG, "Account not found: " + accountId);
                    return;
                }
                
                repository.syncEmails(account, new EmailRepository.SyncCallback() {
                    @Override
                    public void onSuccess(int count) {
                        Log.d(TAG, "Synced " + count + " new emails for account: " + account.getEmail());
                        if (count > 0) {
                            showNotification(account.getEmail(), count);
                        }
                    }
                    
                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Sync failed for account " + account.getEmail() + ": " + error);
                    }
                });
                
            } catch (Exception e) {
                Log.e(TAG, "Sync error for account " + accountId, e);
            }
            
            Log.d(TAG, "Sync completed for account: " + accountId);
        });
    }
    
    /**
     * 同步所有账户
     */
    private void syncAllAccounts() {
        executor.execute(() -> {
            Log.d(TAG, "Start syncing all accounts");
            
            try {
                List<EmailAccount> accounts = repository.getAllAccounts();
                if (accounts == null || accounts.isEmpty()) {
                    Log.d(TAG, "No email accounts configured");
                    return;
                }
                
                for (EmailAccount account : accounts) {
                    try {
                        final int[] totalCount = {0};
                        repository.syncEmails(account, new EmailRepository.SyncCallback() {
                            @Override
                            public void onSuccess(int count) {
                                Log.d(TAG, "Synced " + count + " new emails for " + account.getEmail());
                                totalCount[0] += count;
                                if (count > 0) {
                                    showNotification(account.getEmail(), count);
                                }
                            }
                            
                            @Override
                            public void onError(String error) {
                                Log.e(TAG, "Sync failed for " + account.getEmail() + ": " + error);
                            }
                        });
                    } catch (Exception e) {
                        Log.e(TAG, "Error syncing account " + account.getEmail(), e);
                    }
                }
                
            } catch (Exception e) {
                Log.e(TAG, "Sync all accounts error", e);
            }
            
            Log.d(TAG, "Sync all accounts completed");
        });
    }
    
    /**
     * 创建通知渠道
     */
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    CHANNEL_ID,
                    "邮件同步",
                    NotificationManager.IMPORTANCE_LOW
            );
            channel.setDescription("新邮件通知");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }
    }
    
    /**
     * 显示新邮件通知
     */
    private void showNotification(String accountEmail, int count) {
        try {
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_notification)
                    .setContentTitle("收到 " + count + " 封新邮件")
                    .setContentText("账户: " + accountEmail)
                    .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                    .setAutoCancel(true);
            
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (manager != null) {
                manager.notify((int) System.currentTimeMillis(), builder.build());
            }
        } catch (Exception e) {
            Log.e(TAG, "Failed to show notification", e);
        }
    }
    
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
    
    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
