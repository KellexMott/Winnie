package com.techart.winnie;

import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;

import com.techart.winnie.constants.Constants;

import java.util.List;

public class ActivityOverview extends AppCompatActivity {

    private List<String> contents;
    private List<String> chapterTitles;
    private List<String> pageNumbers;
    private int pageCount;
    private int lastPage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_overview);
        ImageView background = findViewById(R.id.iv_news);


        background.setBackground(ContextCompat.getDrawable(ActivityOverview.this,R.drawable.romance2));
        contents = getIntent().getStringArrayListExtra(Constants.POST_CONTENT);
        chapterTitles = getIntent().getStringArrayListExtra(Constants.POST_TITLE);
        pageCount = contents.size();
    }
}
