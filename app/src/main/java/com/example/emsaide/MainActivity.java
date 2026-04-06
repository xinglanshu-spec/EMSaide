package com.example.emsaide;

import android.os.Bundle;
import android.view.MenuItem;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.navigation.NavigationView;

/**
 * 主 Activity
 */
public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    
    private DrawerLayout drawerLayout;
    private NavController navController;
    private AppBarConfiguration appBarConfiguration;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        // 设置 Toolbar
        androidx.appcompat.widget.Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        
        // 设置 DrawerLayout
        drawerLayout = findViewById(R.id.drawerLayout);
        NavigationView navigationView = findViewById(R.id.navigationView);
        navigationView.setNavigationItemSelectedListener(this);
        
        // 设置抽屉开关指示器
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.open_drawer, R.string.close_drawer
        );
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        
        // 获取 NavController
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager()
                .findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment != null) {
            navController = navHostFragment.getNavController();
            
            // 配置 AppBar - 只在顶层页面显示抽屉菜单
            appBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.contactListFragment)
                    .setDrawerLayout(drawerLayout)
                    .build();
            
            NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        }
    }
    
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        
        if (itemId == R.id.nav_contacts) {
            // 已经是当前页面
            drawerLayout.closeDrawers();
        } else if (itemId == R.id.nav_email_accounts) {
            // TODO: 打开邮箱账户列表（未来实现）
            drawerLayout.closeDrawers();
        } else if (itemId == R.id.nav_settings) {
            // 打开邮箱设置
            navController.navigate(R.id.action_contactList_to_emailSettings);
            drawerLayout.closeDrawers();
        }
        
        return true;
    }
    
    @Override
    public boolean onSupportNavigateUp() {
        if (navController != null) {
            return navController.navigateUp() || super.onSupportNavigateUp();
        }
        return super.onSupportNavigateUp();
    }
    
    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }
}