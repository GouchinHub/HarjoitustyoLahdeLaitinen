package com.example.harjoitustyo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.FragmentManager;

import com.google.android.material.navigation.NavigationView;
import com.google.gson.Gson;

import java.io.IOException;

public class MainActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {
    private DrawerLayout drawer;
    private FragmentManager manager;
    private ProfileFragment profileFragment;
    private FitnessFragment fitnessFragment;
    private LifestyleFragment lifestyleFragment;
    private NutritionFragment nutritionFragment;
    private ImageView headerProfilePicture;
    private static final LoginManager loginManager = LoginManager.getInstance();
    private static final ProfileManager profileManager = ProfileManager.getInstance();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //User activeUser = (User) getIntent().getSerializableExtra("user");
        User activeUser = loginManager.getActiveUser();
        drawer = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        //create new fragment and commit to manager
        profileFragment = new ProfileFragment();
        fitnessFragment = new FitnessFragment();
        lifestyleFragment = new LifestyleFragment();
        nutritionFragment = new NutritionFragment();
        //Get the last active view user was in, and load corresponding fragment(when saved instance is null).
        int viewState = activeUser.getViewState();
        manager = getSupportFragmentManager();
        if (savedInstanceState == null) {
            switch (viewState) {
                case 0:
                    manager.beginTransaction().replace(R.id.fragment_container,
                            profileFragment).commit();
                    navigationView.setCheckedItem(R.id.nav_profile);
                    break;
                case 1:
                    manager.beginTransaction().replace(R.id.fragment_container,
                            fitnessFragment).commit();
                    navigationView.setCheckedItem(R.id.nav_fitness);
                    break;
                case 2:
                    manager.beginTransaction().replace(R.id.fragment_container,
                            lifestyleFragment).commit();
                    navigationView.setCheckedItem(R.id.nav_lifestyle);
                    break;
                case 3:
                    manager.beginTransaction().replace(R.id.fragment_container,
                            nutritionFragment).commit();
                    navigationView.setCheckedItem(R.id.nav_nutrition);
                    break;

            }
        }
        //Change default toolbar to new toolbar with menu button to open the navigation drawer.
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        //Create toggle for opening navigation view from toolbar button.
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
        //Show active username and profile picture in navigation view header.
        TextView headerDisplayUser = navigationView.getHeaderView(0).findViewById(R.id.nameDisplay);
        headerDisplayUser.setText(activeUser.getName());
        headerProfilePicture = navigationView.getHeaderView(0).findViewById(R.id.profilePicHeader);
        if (profileManager.getActiveProfile().getPictureUri() != null) {
            updateNavPicture();
        }
        //Set onclick listener to logout button in navigation header.
        Button logoutButton = navigationView.getHeaderView(0).findViewById(R.id.logout_button);
        logoutButton.setOnClickListener(v -> {
            logoutUser();
        });
    }

    //if main activity is paused, active user data is saved.
    //So when app is reopened, it will load the last active user and its current active view.
    @Override
    protected void onPause() {
        if (loginManager.getActiveUser() != null) {
            if (!loginManager.getActiveUser().isGuestFlag()) {
                saveProfile();
            }
            saveActiveUser(loginManager.getActiveUser());
        }
        super.onPause();
    }

    //if main activity is destroyed (Application is shut down), active user data is saved.
    //So when app is reopened, it will load the last active user and its current active view.
    //If active user is guest user, its data will be deleted instead.
    @Override
    protected void onDestroy() {
        if (loginManager.getActiveUser() != null) {
            if (!loginManager.getActiveUser().isGuestFlag()) {
                saveProfile();
            } else {
                //don't delete user data if onDestroy is called because of changed configuration(rotating device)
                if (!isChangingConfigurations()) {
                    profileFragment.deleteGuest();
                    saveActiveUser(null);
                    loginManager.userLogout();
                }
            }
            saveActiveUser(loginManager.getActiveUser());
        }
        super.onDestroy();
    }

    //Load fragment selected in navigation menu and update user view state.
    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.nav_profile:
                manager.beginTransaction().replace(R.id.fragment_container,
                        profileFragment).commit();
                loginManager.changeUserViewState(0);
                break;
            case R.id.nav_fitness:
                manager.beginTransaction().replace(R.id.fragment_container,
                        fitnessFragment).commit();
                loginManager.changeUserViewState(1);
                break;
            case R.id.nav_lifestyle:
                manager.beginTransaction().replace(R.id.fragment_container,
                        lifestyleFragment).commit();
                loginManager.changeUserViewState(2);
                break;
            case R.id.nav_nutrition:
                manager.beginTransaction().replace(R.id.fragment_container,
                        nutritionFragment).commit();
                loginManager.changeUserViewState(3);
                break;

        }
        return true;
    }

    //For closing the navigation drawer
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    //Method saves profile, and set active user to null. Then loads login activity.
    public void logoutUser() {
        saveProfile();
        saveActiveUser(null);
        //method sets active user in login manager to null.
        loginManager.userLogout();
        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
        startActivity(intent);
    }

    //Method gets users profile pic from scoped storage and sets profile picture in navigation menu header to match the users profile picture
    public void updateNavPicture() {
        Uri imageUri = Uri.parse(profileManager.getActiveProfile().getPictureUri());
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(MainActivity.this.getContentResolver(), imageUri);
            headerProfilePicture.setImageBitmap(bitmap);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //Save profile data, by updating users data in shared preferences
    public void saveProfile() {
        SharedPreferences sharedPreferences = getSharedPreferences("user data", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(loginManager.getUsers());
        editor.putString("users", json);
        editor.apply();
    }

    //Save/update active user data into shared preferences
    public void saveActiveUser(User user) {
        SharedPreferences sharedPreferences = getSharedPreferences("Logged user", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(user);
        editor.putString("user", json);
        editor.apply();
    }
}