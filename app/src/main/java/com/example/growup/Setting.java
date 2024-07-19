package com.example.growup;

import android.app.Activity;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

public class Setting {

    public static void initializeDrawerLayout(Activity activity){
        DrawerLayout drawerLayout = MainActivity.activity.findViewById(R.id.drawer_layout);
        NavigationView navigationView = activity.findViewById(R.id.navigationView);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                activity, drawerLayout, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        MenuItem menuItem1 = navigationView.getMenu().findItem(R.id.navMenuItem1);
        String appVersion = BuildConfig.VERSION_NAME;
        SpannableString centeredText = new SpannableString("Version: " + appVersion);
        centeredText.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, appVersion.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        menuItem1.setTitle(centeredText);
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            drawerLayout.openDrawer(GravityCompat.START);
        }
        AppCompatButton app_setting_back_button = MainActivity.activity.findViewById(R.id.setting_button);
        app_setting_back_button.setOnClickListener(view -> {
            if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                drawerLayout.closeDrawer(GravityCompat.START);
            } else {
                drawerLayout.openDrawer(GravityCompat.START);
            }
        });
    }

    public static void setListenerForSettingButton(){
        AppCompatButton app_setting_back_button = MainActivity.activity.findViewById(R.id.setting_button);
        app_setting_back_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (MainActivity.currentId == 0){
                    Setting.initializeDrawerLayout(MainActivity.activity);
                }else{
                    MainActivity.backButtonProcess();
                }
            }
        });
    }


}
