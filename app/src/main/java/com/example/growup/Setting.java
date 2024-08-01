package com.example.growup;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.AlignmentSpan;
import android.view.ActionProvider;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

public class Setting {

    public static void initializeDrawerLayout(Activity activity){
        DrawerLayout drawerLayout = activity.findViewById(R.id.drawer_layout);
        NavigationView navigationView = activity.findViewById(R.id.navigationView);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(
                activity, drawerLayout, R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(actionBarDrawerToggle);
        actionBarDrawerToggle.syncState();

        MenuItem versionItem = navigationView.getMenu().findItem(R.id.navMenuItem1);
        String appVersion = BuildConfig.VERSION_NAME;
        SpannableString centeredText = new SpannableString("Version: " + appVersion);
        centeredText.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, appVersion.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        versionItem.setTitle(centeredText);


        MenuItem syncToGoogleDriveItem = navigationView.getMenu().findItem(R.id.navMenuItem2);
        String gmail_text = "Sync To Google Drive";
        if (MainActivity.isLinkedToGoogleDrive){
            gmail_text = DBHelper.getAccount().getUserEmail() + "@gmail.com";
        }
        centeredText = new SpannableString(gmail_text);
        centeredText.setSpan(new AlignmentSpan.Standard(Layout.Alignment.ALIGN_CENTER), 0, appVersion.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
        syncToGoogleDriveItem.setTitle(centeredText);


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

    public static void setListenerForButtons(){
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

        Button sync_to_google_drive_button = MainActivity.activity.findViewById(R.id.sync_button);
        sync_to_google_drive_button.setOnClickListener(view -> {

            if (MainActivity.isLinkedToGoogleDrive){
                MainActivity.activity.runOnUiThread(() -> {
                    Toast.makeText(MainActivity.activity, "you already have login to google", Toast.LENGTH_SHORT).show();});
                return;
            }
            MainActivity.activity.runOnUiThread(() -> {
                Toast.makeText(MainActivity.activity, "Please Wait To Load Google Page", Toast.LENGTH_SHORT).show();});
            MainActivity.googleCloud.signInToGoogleCloud(MainActivity.signInToBackUpLauncher)
            ;});
    }

    }
