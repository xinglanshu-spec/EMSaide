package com.example.emsaide.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 邮件同步服务
 * 用于后台同步邮件
 */
public class SyncService extends Service {
    
    private static final String TAG = "SyncService";
    public static final String ACTION_SYNC = "com.example.emsaide.action.SYNC";
    public static final String EXTRA_ACCOUNT_ID = "account_id";
    
    private ExecutorService executor;
    
    @Override
    public void onCreate() {
        super.onCreate();
        executor = Executors.newSingleThreadExecutor();
    }
    
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_SYNC.equals(intent.getAction())) {
            long accountId = intent.getLongExtra(EXTRA_ACCOUNT_ID, -1);
            if (accountId != -1) {
                syncAccount(accountId);
            }
        }
        return START_NOT_STICKY;
    }
    
    private void syncAccount(long accountId) {
        executor.execute(() -> {
            Log.d(TAG, "Start syncing account: " + accountId);
            
            // TODO: 实现具体的同步逻辑
            // 这里可以使用 EmailRepository 来同步邮件
            
            Log.d(TAG, "Sync completed for account: " + accountId);
        });
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
