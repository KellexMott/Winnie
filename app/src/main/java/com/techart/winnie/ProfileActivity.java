package com.techart.winnie;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
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
public class ProfileActivity extends AppCompatActivity {
    private String userName;
    private static String author;
    private FirebaseAuth mAuth;
    private Button btPost;

    private static final int GALLERY_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        author = FireBaseUtils.getAuthor();
        btPost = findViewById(R.id.btPost);
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            userName = user.getDisplayName();
        }
        setTitle(userName);

        btPost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent post = new Intent(ProfileActivity.this,StoryViewActivity.class);
                startActivity(post);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_account, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            logOut();
        }else if (id == R.id.action_create) {
            Intent post = new Intent(ProfileActivity.this,SettingsActivity.class);
            startActivity(post);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
    }

    private void logOut() {
        DialogInterface.OnClickListener dialogClickListener =
        new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int button) {
            if (button == DialogInterface.BUTTON_POSITIVE)
            {
                FirebaseAuth.getInstance().signOut();
            }
            }
        };
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(getString(R.string.are_you_sure))
            .setPositiveButton(getString(android.R.string.yes), dialogClickListener)
            .setNegativeButton(getString(android.R.string.no), dialogClickListener)
            .show();
    }
}
