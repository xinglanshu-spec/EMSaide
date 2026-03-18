package com.example.emsaide.ui;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emsaide.R;
import com.example.emsaide.data.model.ChatMessage;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;

import java.util.ArrayList;
import java.util.List;

/**
 * 聊天详情界面
 */
public class ChatDetailFragment extends Fragment {
    
    private RecyclerView messageRecyclerView;
    private View emptyMessageView;
    private EditText messageInput;
    private MaterialButton sendButton;
    
    private MessageListAdapter adapter;
    private ChatDetailViewModel viewModel;
    private long accountId;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_detail, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        messageRecyclerView = view.findViewById(R.id.messageRecyclerView);
        emptyMessageView = view.findViewById(R.id.emptyMessageView);
        messageInput = view.findViewById(R.id.messageInput);
        sendButton = view.findViewById(R.id.sendButton);
        
        // 获取账户 ID
        if (getArguments() != null) {
            accountId = getArguments().getLong("accountId", -1);
        }
        
        // 初始化 RecyclerView
        adapter = new MessageListAdapter(new ArrayList<>());
        messageRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        messageRecyclerView.setAdapter(adapter);
        
        // 初始化 ViewModel
        viewModel = new ViewModelProvider(this).get(ChatDetailViewModel.class);
        
        // 加载消息
        viewModel.loadMessages(accountId);
        
        // 观察消息数据
        viewModel.getMessages().observe(getViewLifecycleOwner(), messages -> {
            if (messages != null && !messages.isEmpty()) {
                adapter.setMessages(messages);
                messageRecyclerView.setVisibility(View.VISIBLE);
                emptyMessageView.setVisibility(View.GONE);
                
                // 滚动到底部
                messageRecyclerView.scrollToPosition(messages.size() - 1);
                
                // 标记为已读
                viewModel.markAllAsRead();
            } else {
                messageRecyclerView.setVisibility(View.GONE);
                emptyMessageView.setVisibility(View.VISIBLE);
            }
        });
        
        // 同步按钮（长按）
        sendButton.setOnLongClickListener(v -> {
            showSyncDialog();
            return true;
        });
        
        // 发送按钮点击
        sendButton.setOnClickListener(v -> sendMessage());
    }
    
    private void sendMessage() {
        String content = messageInput.getText().toString().trim();
        if (content.isEmpty()) {
            return;
        }
        
        viewModel.sendEmail(content, new ChatDetailViewModel.SendCallback() {
            @Override
            public void onSuccess() {
                messageInput.setText("");
                Snackbar.make(requireView(), "邮件已发送", Snackbar.LENGTH_SHORT).show();
            }
            
            @Override
            public void onError(String error) {
                Snackbar.make(requireView(), "发送失败：" + error, Snackbar.LENGTH_LONG).show();
            }
        });
    }
    
    private void showSyncDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.sync)
                .setMessage("确定要同步新邮件吗？")
                .setPositiveButton(R.string.ok, (dialog, which) -> {
                    viewModel.syncEmails(new ChatDetailViewModel.SyncCallback() {
                        @Override
                        public void onSuccess(int count) {
                            Snackbar.make(requireView(), 
                                getString(R.string.sync_complete, count), 
                                Snackbar.LENGTH_SHORT).show();
                        }
                        
                        @Override
                        public void onError(String error) {
                            Snackbar.make(requireView(), 
                                getString(R.string.sync_error, error), 
                                Snackbar.LENGTH_LONG).show();
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }
}
