package com.techart.winnie;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.techart.winnie.constants.Constants;
import com.techart.winnie.constants.FireBaseUtils;
import com.techart.winnie.models.Chapter;
import com.techart.winnie.models.Story;
import com.techart.winnie.utilities.ImageUtils;
import com.techart.winnie.utilities.TimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Displays users private content. Such as
 * 1. Posted items
 * 2. Locally stored Articles
 * 3. Action such as changing and setting of dps
 */
public class ProfileViewActivity extends AppCompatActivity {
    private String userName;
    private static String author;

    private RecyclerView mStoryGridView;
    private DatabaseReference mDatabaseChapters;
    private boolean mProcessView = false;
    private ArrayList<String> contents;
    private ArrayList<String> chapterTitles;
    private int pageCount;
    private long timeAccessed;
    private RecyclerView.LayoutManager recyclerViewLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profileview);
        author = FireBaseUtils.getAuthor();
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            // Name, email address, and profile photo Url
            userName = user.getDisplayName();
        }
        setTitle(userName);

        mStoryGridView = findViewById(R.id.poem_list);
        mStoryGridView.setHasFixedSize(true);

        recyclerViewLayoutManager = new GridLayoutManager(ProfileViewActivity.this,2);
        mStoryGridView.setLayoutManager(recyclerViewLayoutManager);
        bindView();
    }

    private void bindView() {
        Query storyQuery = FireBaseUtils.mDatabaseStory.child(author).orderByChild(Constants.TIME_CREATED);
        FirebaseRecyclerAdapter<Story,PoemViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Story, PoemViewHolder>(
                Story.class,R.layout.item_storyview,PoemViewHolder.class, storyQuery)
        {
            @Override
            protected void populateViewHolder(PoemViewHolder viewHolder, final Story model, int position) {
                final String post_key = getRef(position).getKey();
                viewHolder.tvStatus.setText(getString(R.string.post_status,model.getStatus()));
                viewHolder.setIvImage(ProfileViewActivity.this, ImageUtils.getStoryUrl(model.getCategory().trim()));
                if (model.getNumLikes() != null) {
                    viewHolder.numLikes.setText(String.format("%s",model.getNumLikes().toString()));
                }

                if (model.getNumComments() != null) {
                    viewHolder.numComments.setText(String.format("%s",model.getNumComments().toString()));
                }

                if (model.getNumViews() != null) {
                    viewHolder.tvNumViews.setText(String.format("%s",model.getNumViews().toString()));
                }

                if (model.getTimeCreated() != null) {
                    String time = TimeUtils.timeElapsed(model.getTimeCreated());
                    viewHolder.timeTextView.setText(time);
                }

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        FirebaseMessaging.getInstance().subscribeToTopic(post_key);
                        addToViews(post_key, model);
                        initializeChapters(post_key, model);
                    }
                });
            }
        };
        mStoryGridView.setAdapter(firebaseRecyclerAdapter);
        firebaseRecyclerAdapter.notifyDataSetChanged();
    }

    private void addToViews(final String post_key, final Story model) {
        mProcessView = true;
        FireBaseUtils.mDatabaseViews.child(post_key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (mProcessView) {
                    if (!dataSnapshot.hasChild(FireBaseUtils.mAuth.getCurrentUser().getUid())) {
                        FireBaseUtils.addStoryView(model,post_key);
                        mProcessView = false;
                        FireBaseUtils.onStoryViewed(post_key);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initializeChapters(String post_key, Story model) {
        mDatabaseChapters = FireBaseUtils.mDatabaseChapters.child(post_key);
        contents = new ArrayList<>();
        chapterTitles = new ArrayList<>();
        addToLibrary(model,post_key);
        loadChapters();
    }

    private void loadChapters() {
        final ProgressDialog progressDialog = new ProgressDialog(ProfileViewActivity.this);
        progressDialog.setMessage("Loading chapters");
        progressDialog.setCancelable(true);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        mDatabaseChapters.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                pageCount = ((int) dataSnapshot.getChildrenCount());
                for (DataSnapshot chapterSnapShot: dataSnapshot.getChildren()) {
                    Chapter chapter = chapterSnapShot.getValue(Chapter.class);
                    contents.add(chapter.getContent());
                    chapterTitles.add(chapter.getChapterTitle());
                }
                if (contents.size() == pageCount) {
                    progressDialog.dismiss();
                    Intent readIntent = new Intent(ProfileViewActivity.this,ActivityRead.class);
                    readIntent.putStringArrayListExtra(Constants.POST_CONTENT,contents);
                    readIntent.putStringArrayListExtra(Constants.POST_TITLE,chapterTitles);
                    startActivity(readIntent);
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }


    private void addToLibrary(final Story model, final String post_key)
    {
        FireBaseUtils.mDatabaseLibrary.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (!dataSnapshot.child(FireBaseUtils.mAuth.getCurrentUser().getUid()).hasChild(post_key)) {
                    Map<String,Object> values = new HashMap<>();
                    values.put(Constants.POST_KEY,  post_key);
                    values.put(Constants.POST_TITLE, model.getTitle());
                    values.put("lastAccessed", timeAccessed);
                    FireBaseUtils.mDatabaseLibrary.child(FireBaseUtils.mAuth.getCurrentUser().getUid()).child(post_key).setValue(values);
                    Toast.makeText(ProfileViewActivity.this,model.getTitle() + " added to library",Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }

    public static class PoemViewHolder extends RecyclerView.ViewHolder {
        TextView post_title;
        TextView tvStatus;
        TextView numLikes;
        TextView numComments;
        TextView tvNumViews;
        ImageView ivArticle;

        TextView timeTextView;
        View mView;

        public PoemViewHolder(View itemView) {
            super(itemView);
            ivArticle = itemView.findViewById(R.id.iv_news);
            post_title = itemView.findViewById(R.id.post_title);

            numLikes = itemView.findViewById(R.id.tv_likes);
            tvNumViews = itemView.findViewById(R.id.tv_numviews);

            tvStatus = itemView.findViewById(R.id.tv_status);
            numComments = itemView.findViewById(R.id.tv_comments);

            timeTextView = itemView.findViewById(R.id.tvTime);

            this.mView = itemView;
        }

        public void setIvImage(Context context, int image)
        {
            Glide.with(context)
                    .load(image)
                    .into(ivArticle);
        }
    }
}