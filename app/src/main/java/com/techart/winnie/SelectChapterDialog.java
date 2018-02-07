package com.techart.winnie;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import java.util.ArrayList;
import java.util.List;
/**
 * Presents Story Edit options
 * Created by Kelvin on 30/07/2017.
 */

public class SelectChapterDialog extends AppCompatActivity {

    private String chapters;

    private List<String> pageNumbers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.chapter_dialog);
        GridView gridView =  findViewById(R.id.gv_chapters);
        pageNumbers = new ArrayList<>();
        for(int i = 1; i <= 20; i++)
        {
            pageNumbers.add(String.valueOf(i));
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this,
                android.R.layout.simple_list_item_1, pageNumbers);
        gridView.setAdapter(adapter);

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Intent chapter = new Intent();
                chapter.setData(Uri.parse(String.valueOf(i+1)));
                setResult(RESULT_OK,chapter);
                finish();
            }
        });
                /*
        chapters = getIntent().getStringExtra(Constants.STORY_CHAPTERCOUNT);*/
    }
}
