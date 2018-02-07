package com.techart.winnie;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.techart.winnie.constants.Constants;
import com.techart.winnie.constants.FireBaseUtils;
import com.techart.winnie.models.Chapter;


public class ChapterListActivity extends AppCompatActivity {
    private RecyclerView mPoemList;
    private String storyUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.tabrecyclerviewer);

        storyUrl = getIntent().getStringExtra(Constants.STORY_REFID);
        setTitle("Chapters");

        mPoemList = (RecyclerView) findViewById(R.id.poem_list);
        mPoemList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(ChapterListActivity.this);
        mPoemList.setLayoutManager(linearLayoutManager);
        bindView();
    }

    private void bindView()
    {
        FirebaseRecyclerAdapter<Chapter,ChapterViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Chapter, ChapterViewHolder>(
                Chapter.class,R.layout.item_chapter,ChapterViewHolder.class, FireBaseUtils.mDatabaseChapters.child(storyUrl)) {
            @Override
            protected void populateViewHolder(ChapterViewHolder viewHolder, final Chapter model, int position) {
                final String post_key = getRef(position).getKey();
                viewHolder.tvTitle.setText(model.getChapterTitle());
                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent readIntent = new Intent(ChapterListActivity.this,ChapterEditorActivity.class);
                        readIntent.putExtra(Constants.POST_KEY,post_key);
                        readIntent.putExtra(Constants.STORY_REFID,storyUrl);
                        readIntent.putExtra(Constants.CHAPTER_TITLE,model.getChapterTitle());
                        readIntent.putExtra(Constants.CHAPTER_CONTENT,model.getContent());
                        startActivity(readIntent);
                    }
                });

            }
        };
        mPoemList.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();
    }

    @Override
    public void onBackPressed()
    {
        setResult(RESULT_OK,getIntent());
        finish();
    }

    public static class ChapterViewHolder extends RecyclerView.ViewHolder
    {
        TextView tvTitle;
        TextView tvTime;
        View mView;

        public ChapterViewHolder(View itemView) {
            super(itemView);
            tvTitle = (TextView)itemView.findViewById(R.id.tvTitle);
            tvTime = (TextView) itemView.findViewById(R.id.tv_timeCreated);
            this.mView = itemView;
       }
    }

}

