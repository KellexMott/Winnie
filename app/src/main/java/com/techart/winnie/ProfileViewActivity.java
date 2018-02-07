package com.techart.winnie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.techart.winnie.constants.FireBaseUtils;

/**
 * Displays users private content. Such as
 * 1. Posted items
 * 2. Locally stored Articles
 * 3. Action such as changing and setting of dps
 */
public class ProfileViewActivity extends AppCompatActivity {
    private String userName;
    private static String author;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        author = FireBaseUtils.getAuthor();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            userName = user.getDisplayName();
        }
        setTitle(userName);
    }
}
