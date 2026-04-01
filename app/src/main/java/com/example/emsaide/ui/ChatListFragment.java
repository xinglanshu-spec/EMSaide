package com.example.emsaide.ui;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.emsaide.R;
import com.example.emsaide.data.database.AppDatabase;
import com.example.emsaide.data.model.EmailAccount;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 聊天列表界面
 */
public class ChatListFragment extends Fragment {
    
    private static final String TAG = "ChatListFragment";
    
    private RecyclerView chatRecyclerView;
    private View emptyView;
    private FloatingActionButton addAccountFab;
    
    private ChatListAdapter adapter;
    private AppDatabase database;
    private ExecutorService executor;
    
    private OnChatSelectedListener listener;
    
    public interface OnChatSelectedListener {
        void onChatSelected(long accountId);
    }
    
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnChatSelectedListener) {
            listener = (OnChatSelectedListener) context;
        }
    }
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_chat_list, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        chatRecyclerView = view.findViewById(R.id.chatRecyclerView);
        emptyView = view.findViewById(R.id.emptyView);
        addAccountFab = view.findViewById(R.id.addAccountFab);
        
        // 初始化和显示空视图
        emptyView.setVisibility(View.VISIBLE);
        chatRecyclerView.setVisibility(View.GONE);
        
        // 初始化数据库和执行器
        database = AppDatabase.getInstance(requireContext());
        executor = Executors.newSingleThreadExecutor();
        
        // 初始化 RecyclerView
        adapter = new ChatListAdapter(new ArrayList<>(), this::onAccountClick);
        chatRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        chatRecyclerView.setAdapter(adapter);
        
        // 加载账户列表
        loadAccounts();
        
        // 添加按钮点击事件 - 暂时禁用，因为主界面已改为联系人列表
        // 将来可以通过侧边栏菜单访问邮箱账户管理
        addAccountFab.setVisibility(View.GONE);
    }
    
    private void loadAccounts() {
        executor.execute(() -> {
            try {
                List<EmailAccount> accounts = database.emailAccountDao().getAllAccounts();
                Log.d(TAG, "Loaded " + accounts.size() + " accounts");
                
                // 打印所有账户信息用于调试
                for (EmailAccount account : accounts) {
                    Log.d(TAG, "Account: " + account.getEmail() + " - " + account.getAccountName());
                }
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Log.d(TAG, "Updating UI with " + accounts.size() + " accounts");
                        if (accounts != null && !accounts.isEmpty()) {
                            adapter.setAccounts(accounts);
                            chatRecyclerView.setVisibility(View.VISIBLE);
                            emptyView.setVisibility(View.GONE);
                            Log.d(TAG, "Showing RecyclerView");
                        } else {
                            chatRecyclerView.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                            Log.d(TAG, "Showing EmptyView");
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Error loading accounts", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        chatRecyclerView.setVisibility(View.GONE);
                        emptyView.setVisibility(View.VISIBLE);
                    });
                }
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        // 返回时刷新列表
        loadAccounts();
    }
    
    private void onAccountClick(EmailAccount account) {
        if (listener != null) {
            listener.onChatSelected(account.getId());
        } else {
            // 如果没有监听器，使用 Navigation
            Bundle bundle = new Bundle();
            bundle.putLong("accountId", account.getId());
            Navigation.findNavController(requireView())
                    .navigate(R.id.chatDetailFragment, bundle);
        }
    }
}
