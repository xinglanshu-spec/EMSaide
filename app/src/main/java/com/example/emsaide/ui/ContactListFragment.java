package com.example.emsaide.ui;

import android.os.Bundle;
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
import com.example.emsaide.data.model.Contact;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 联系人列表界面
 */
public class ContactListFragment extends Fragment {
    
    private RecyclerView contactRecyclerView;
    private View emptyView;
    private FloatingActionButton addContactFab;
    
    private ContactListAdapter adapter;
    private AppDatabase database;
    private ExecutorService executor;
    
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_contact_list, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        contactRecyclerView = view.findViewById(R.id.contactRecyclerView);
        emptyView = view.findViewById(R.id.emptyView);
        addContactFab = view.findViewById(R.id.addContactFab);
        
        // 初始化
        emptyView.setVisibility(View.VISIBLE);
        contactRecyclerView.setVisibility(View.GONE);
        
        database = AppDatabase.getInstance(requireContext());
        executor = Executors.newSingleThreadExecutor();
        
        // 初始化 RecyclerView
        adapter = new ContactListAdapter(new ArrayList<>(), this::onContactClick);
        contactRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        contactRecyclerView.setAdapter(adapter);
        
        // 加载联系人
        loadContacts();
        
        // 添加按钮
        addContactFab.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_contactList_to_addContact);
        });
    }
    
    private void loadContacts() {
        executor.execute(() -> {
            try {
                List<Contact> contacts = database.contactDao().getAllContacts();
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (contacts != null && !contacts.isEmpty()) {
                            adapter.setContacts(contacts);
                            contactRecyclerView.setVisibility(View.VISIBLE);
                            emptyView.setVisibility(View.GONE);
                        } else {
                            contactRecyclerView.setVisibility(View.GONE);
                            emptyView.setVisibility(View.VISIBLE);
                        }
                    });
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
    
    @Override
    public void onResume() {
        super.onResume();
        loadContacts();
    }
    
    private void onContactClick(Contact contact) {
        // 打开聊天界面
        Bundle bundle = new Bundle();
        bundle.putLong("contactId", contact.getId());
        bundle.putString("contactName", contact.getName());
        bundle.putString("contactEmail", contact.getEmail());
        Navigation.findNavController(requireView())
                .navigate(R.id.action_contactList_to_chatDetail, bundle);
    }
}
