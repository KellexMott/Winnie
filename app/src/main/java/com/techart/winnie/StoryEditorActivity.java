package com.techart.winnie;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ServerValue;
import com.techart.winnie.constants.Constants;
import com.techart.winnie.constants.FireBaseUtils;
import com.techart.winnie.utilities.EditorUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * Provides the ability to write a Story, Store in a local db and/or post
 * to Server. Only called once per Story.
 * Invoked by StoryDescriptionActivity
 */
public class StoryEditorActivity extends AppCompatActivity {

    private ProgressDialog mProgress;
    private DatabaseReference mDatabaseStory;
    private FirebaseAuth mAuth;

    private DatabaseReference mDatabaseChapters;

    private String chapterUrl = "null";

    private EditText editor;
    private String action;
    private String storyTitle;
    private String storyCategory;
    private String storyDescription;
    String storyUrl ="null";

    String newText;
    String newTitle;
    private String status = "Ongoing";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_story);
        editor = findViewById(R.id.editText);
        mAuth = FirebaseAuth.getInstance();
        storyTitle = getIntent().getStringExtra("Title");
        storyDescription = getIntent().getStringExtra("Description");
        storyCategory = getIntent().getStringExtra("Category");
        action = Intent.ACTION_INSERT;
        setTitle("Adding Chapter 1");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        newText = editor.getText().toString().trim();
        int id = item.getItemId();
        switch (id) {
            case R.id.action_post:
                if (EditorUtils.validateMainText(this,editor.getLayout().getLineCount()))
                {
                    postStory();
                }
                break;
            case R.id.action_delete:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void postStory()
    {
        mProgress = new ProgressDialog(this);
        mDatabaseStory = FireBaseUtils.mDatabaseStory;
        mProgress.setMessage("Posting ...");
        mProgress.show();
        DatabaseReference newPost = mDatabaseStory.push();
        storyUrl = newPost.getKey();
        Map<String,Object> values = new HashMap<>();
        values.put(Constants.STORY_TITLE,storyTitle);
        values.put(Constants.STORY_DESCRIPTION,storyDescription);
        values.put(Constants.STORY_CATEGORY,storyCategory);
        values.put(Constants.STORY_STATUS,status);
        values.put(Constants.STORY_CHAPTERCOUNT,0);
        values.put(Constants.NUM_LIKES,0);
        values.put(Constants.NUM_COMMENTS,0);
        values.put(Constants.NUM_VIEWS,0);
       // values.put(Constants.AUTHOR_URL,mAuth.getCurrentUser().getUid());
        values.put(Constants.POST_AUTHOR,getAuthor());
        values.put(Constants.TIME_CREATED,ServerValue.TIMESTAMP);
        mDatabaseStory.child(storyUrl).setValue(values);
        FireBaseUtils.subscribeTopic(storyUrl);
        postStoryChapter();
        Toast.makeText(getApplicationContext(),"Story successfully posted", Toast.LENGTH_LONG).show();
    }

    private void postStoryChapter()
    {
        mDatabaseChapters = FireBaseUtils.mDatabaseChapters.child(storyUrl);
        chapterUrl = mDatabaseChapters.push().getKey();
        Map<String,Object> values = new HashMap<>();
        values.put(Constants.CHAPTER_TITLE,newTitle);
        values.put(Constants.CHAPTER_CONTENT,newText);
        mDatabaseChapters.child(chapterUrl).setValue(values);
        mProgress.dismiss();

    }

    public String getAuthor()
    {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        return user.getDisplayName();
    }

    @Override
    public void onBackPressed() {

    }
}
