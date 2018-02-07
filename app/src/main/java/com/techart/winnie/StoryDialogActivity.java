package com.techart.winnie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.techart.winnie.constants.Constants;
import com.techart.winnie.constants.FireBaseUtils;
import com.techart.winnie.models.Story;

/**
 * Presents Story Edit options
 * Created by Kelvin on 30/07/2017.
 */

public class StoryDialogActivity extends AppCompatActivity {

    private String postKey;
    private String chapters;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_selectaction);
        ListView list = (ListView) findViewById(R.id.list);
        final String[] options = new String[] { "Add Chapter", "Edit Chapter", "Edit Description", "Edit Title" };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, options);
        list.setAdapter(adapter);
        list.setStackFromBottom(true);
        postKey = getIntent().getStringExtra(Constants.POST_KEY);
        chapters = getIntent().getStringExtra(Constants.STORY_CHAPTERCOUNT);


        list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (options[position]) {
                    case "Add Chapter" :
                        Intent editorDevotionIntent = new Intent(StoryDialogActivity.this, AddChapterActivity.class);
                        editorDevotionIntent.putExtra(Constants.STORY_REFID,postKey);
                        editorDevotionIntent.putExtra(Constants.STORY_CHAPTERCOUNT,chapters);
                        startActivity(editorDevotionIntent);
                        break;
                    case "Edit Chapter":
                        Intent editorStoryIntent = new Intent(StoryDialogActivity.this, ChapterListActivity.class);
                        editorStoryIntent.putExtra(Constants.STORY_REFID,postKey);
                        startActivity(editorStoryIntent);
                        break;
                    case "Edit Description":
                        loadDescription();
                        break;
                    case "Edit Title":
                        loadTitle();
                        break;
                }
            }

        });
    }

    private void loadDescription() {
        FireBaseUtils.mDatabaseStory.child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    Story story = dataSnapshot.getValue(Story.class);
                    Intent readIntent = new Intent(StoryDialogActivity.this,StoryDescriptionEditDialog.class);
                    readIntent.putExtra(Constants.STORY_REFID,postKey);
                    readIntent.putExtra(Constants.STORY_DESCRIPTION,story.getDescription());
                    startActivity(readIntent);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void loadTitle() {
        FireBaseUtils.mDatabaseStory.child(postKey).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                    Story story = dataSnapshot.getValue(Story.class);
                    Intent readIntent = new Intent(StoryDialogActivity.this,StoryTitleEditDialog.class);
                    readIntent.putExtra(Constants.STORY_REFID,postKey);
                    readIntent.putExtra(Constants.STORY_DESCRIPTION,story.getTitle());
                    startActivity(readIntent);
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }
}
