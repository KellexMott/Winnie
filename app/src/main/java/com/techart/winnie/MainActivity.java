package com.techart.winnie;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessaging;
import com.techart.winnie.constants.Constants;
import com.techart.winnie.constants.FireBaseUtils;
import com.techart.winnie.models.Chapter;
import com.techart.winnie.models.Story;
import com.techart.winnie.utilities.EditorUtils;
import com.techart.winnie.utilities.ImageUtils;
import com.techart.winnie.utilities.NumberUtils;
import com.techart.winnie.utilities.TimeUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {
    private RecyclerView mStoryList;
    private DatabaseReference mDatabaseChapters;
    private boolean mProcessLike = false;
    private boolean mProcessView = false;
    private ArrayList<String> contents;
    private ArrayList<String> chapterTitles;
    private int pageCount;
    Long timeAccessed;
    private BottomNavigationView navigation;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_home:

                    return true;
                case R.id.navigation_dashboard:
                   // Intent dialogIntent = new Intent(MainActivity.this,  Library.class);
                    //startActivity(dialogIntent);
                    return true;
                case R.id.navigation_notifications:
                    Intent accountIntent = new Intent(MainActivity.this, ProfileActivity.class);
                    startActivity(accountIntent);
                    return true;
            }
            return false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                if (firebaseAuth.getCurrentUser()==null)
                {
                    Intent loginIntent = new Intent(MainActivity.this,LoginActivity.class);
                    loginIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(loginIntent);
                }
                FirebaseMessaging.getInstance().subscribeToTopic("newPost");
                FireBaseUtils.subscribeToNewPostNotification();
            }
        };
        navigation =  findViewById(R.id.bottom_navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
        FireBaseUtils.mDatabaseLike.keepSynced(true);
        FireBaseUtils.mDatabaseStory.keepSynced(true);
        mStoryList = findViewById(R.id.rv_story);
        mStoryList.setHasFixedSize(true);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setReverseLayout(true);
        linearLayoutManager.setStackFromEnd(true);
        mStoryList.setLayoutManager(linearLayoutManager);
        bindView();
    }

    private void bindView() {
        FirebaseRecyclerAdapter<Story,StoryViewHolder> fireBaseRecyclerAdapter = new FirebaseRecyclerAdapter<Story, StoryViewHolder>(
                Story.class,R.layout.item_storyrow,StoryViewHolder.class, FireBaseUtils.mDatabaseStory)
        {
            @Override
            protected void populateViewHolder(StoryViewHolder viewHolder, final Story model, int position) {
                final String post_key = getRef(position).getKey();
                viewHolder.tvCategory.setText(getString(R.string.post_category,model.getCategory()));
                viewHolder.tvStatus.setText(getString(R.string.post_status,model.getStatus()));
                viewHolder.tvChapters.setText(getString(R.string.post_chapters, NumberUtils.setPlurality(model.getChapters(),"Chapter")));
                viewHolder.setIvImage(MainActivity.this, ImageUtils.getStoryUrl(model.getCategory().trim()));
                viewHolder.setTypeFace(MainActivity.this);
                viewHolder.btAuthor.setText(getString(R.string.post_author,model.getTitle()));
                if (model.getNumLikes() != null)
                {
                    viewHolder.tvNumLikes.setText(String.format("%s",model.getNumLikes().toString()));
                }

                if (model.getNumComments() != null)
                {
                    viewHolder.tvNumComments.setText(String.format("%s",model.getNumComments().toString()));
                }

                if (model.getNumViews() != null)
                {
                    viewHolder.tvNumViews.setText(String.format("%s",model.getNumViews().toString()));
                }

                if (model.getTimeCreated() != null)
                {
                    String time = TimeUtils.timeElapsed(model.getTimeCreated());
                    viewHolder.tvTime.setText(time);
                }

                viewHolder.setLikeBtn(post_key);
                viewHolder.setPostViewed(post_key);

                if (model.getLastUpdate() != null)
                {
                    Boolean t = TimeUtils.currentTime() - model.getLastUpdate() < TimeUtils.MILLISECONDS_DAY; //&& res;
                    viewHolder.setVisibility(t);
                }

                viewHolder.mView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        showDescription(model.getDescription(),post_key,model);
                    }
                });

                viewHolder.btnLiked.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mProcessLike = true;
                        FireBaseUtils.mDatabaseLike.child(post_key).addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (mProcessLike) {
                                    if (dataSnapshot.hasChild(FireBaseUtils.mAuth.getCurrentUser().getUid()))
                                    {
                                        FireBaseUtils.mDatabaseLike.child(post_key).child(FireBaseUtils.mAuth.getCurrentUser().getUid()).removeValue();
                                        FireBaseUtils.onStoryDisliked(post_key);
                                        mProcessLike = false;
                                    } else {
                                        FireBaseUtils.addStoryLike(model,post_key);
                                        mProcessLike = false;
                                        FireBaseUtils.onStoryLiked(post_key);
                                    }
                                }
                            }
                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }
                        });
                    }
                });

                viewHolder.tvNumLikes.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent likedPostsIntent = new Intent(MainActivity.this,LikesActivity.class);
                        likedPostsIntent.putExtra(Constants.POST_KEY,post_key);
                        startActivity(likedPostsIntent);
                    }
                });
                viewHolder.btnComment.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent commentIntent = new Intent(MainActivity.this,CommentActivity.class);
                        commentIntent.putExtra(Constants.POST_KEY,post_key);
                        commentIntent.putExtra(Constants.POST_TITLE,model.getTitle());
                        startActivity(commentIntent);
                    }
                });
                viewHolder.btnViews.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent likedPostsIntent = new Intent(MainActivity.this,ViewsActivity.class);
                        likedPostsIntent.putExtra(Constants.POST_KEY,post_key);
                        startActivity(likedPostsIntent);
                    }
                });
            }
        };
        mStoryList.setAdapter(fireBaseRecyclerAdapter);
        fireBaseRecyclerAdapter.notifyDataSetChanged();
    }

    private void addToViews(final String post_key, final Story model) {
        mProcessView = true;
        FireBaseUtils.mDatabaseViews.child(post_key).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (mProcessView) {
                    if (!dataSnapshot.hasChild(FireBaseUtils.mAuth.getCurrentUser().getUid()))
                    {
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


    private void loadChapters()
    {
        final ProgressDialog progressDialog = new ProgressDialog(MainActivity.this);
        progressDialog.setMessage("Loading chapters");
        progressDialog.setCancelable(true);
        progressDialog.setIndeterminate(true);
        progressDialog.show();
        mDatabaseChapters.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                pageCount = ((int) dataSnapshot.getChildrenCount());
                for (DataSnapshot chapterSnapShot: dataSnapshot.getChildren())
                {
                    Chapter chapter = chapterSnapShot.getValue(Chapter.class);
                    contents.add(chapter.getContent());
                    chapterTitles.add(chapter.getChapterTitle());
                }
                if (contents.size() == pageCount) {
                    progressDialog.dismiss();
                    Intent readIntent = new Intent(MainActivity.this,ActivityRead.class);
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
                if (!dataSnapshot.child(FireBaseUtils.mAuth.getCurrentUser().getUid()).hasChild(post_key))
                {
                    Map<String,Object> values = new HashMap<>();
                    values.put(Constants.POST_KEY,  post_key);
                    values.put(Constants.POST_TITLE, model.getTitle());
                    values.put("lastAccessed", timeAccessed);
                    FireBaseUtils.mDatabaseLibrary.child(FireBaseUtils.mAuth.getCurrentUser().getUid()).child(post_key).setValue(values);
                    Toast.makeText(MainActivity.this,model.getTitle() + " added to library",Toast.LENGTH_LONG).show();
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onResume(){
        super.onResume();
        navigation.setSelectedItemId(R.id.navigation_home);
    }



    @Override
    public String toString() {
        return "Stories";
    }

    private void showDescription(String description, final String post_key, final Story model) {
        DialogInterface.OnClickListener dialogClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int button) {
                        if (button == DialogInterface.BUTTON_POSITIVE)
                        {
                            FirebaseMessaging.getInstance().subscribeToTopic(post_key);
                            addToViews(post_key, model);
                            initializeChapters(post_key, model);
                        }
                        else
                        {
                            dialog.dismiss();
                        }
                    }
                };
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setMessage(description)
                .setPositiveButton("Start Reading", dialogClickListener)
                .setNegativeButton("Back", dialogClickListener)
                .show();
    }

    public static class StoryViewHolder extends RecyclerView.ViewHolder
    {
        TextView tvState;
        Button btAuthor;
        TextView tvCategory;
        TextView tvStatus;
        TextView tvChapters;
        TextView tvNumLikes;
        TextView tvNumComments;
        TextView tvNumViews;
        TextView tvTime;

        ImageView ivStory;
        View mView;

        DatabaseReference mDatabaseLike;
        FirebaseAuth mAUth;

        ImageButton btnLiked;
        ImageButton btnComment;
        ImageButton btnViews;

        public StoryViewHolder(View itemView) {
            super(itemView);
            tvState = itemView.findViewById(R.id.tv_state);
            btAuthor = itemView.findViewById(R.id.bt_author);
            tvStatus = itemView.findViewById(R.id.tv_status);
            tvChapters = itemView.findViewById(R.id.tv_chapters);
            tvCategory = itemView.findViewById(R.id.tv_category);
            ivStory = itemView.findViewById(R.id.iv_news);

            btnLiked = itemView.findViewById(R.id.likeBtn);
            btnComment = itemView.findViewById(R.id.commentBtn);
            btnViews = itemView.findViewById(R.id.bt_views);
            tvTime = itemView.findViewById(R.id.tv_time);
            tvNumLikes = itemView.findViewById(R.id.tv_numlikes);
            tvNumComments = itemView.findViewById(R.id.tv_numcomments);
            tvNumViews = itemView.findViewById(R.id.tv_numviews);
            this.mView = itemView;
            mDatabaseLike = FireBaseUtils.mDatabaseLike;
            mAUth = FirebaseAuth.getInstance();
            mDatabaseLike.keepSynced(true);
        }

        protected void setTypeFace(Context context) {
            Typeface typeface = EditorUtils.getTypeFace(context);
            btAuthor.setTypeface(typeface);
            tvStatus.setTypeface(typeface);
            tvCategory.setTypeface(typeface);
            tvChapters.setTypeface(typeface);
        }

        protected void setVisibility(Boolean isVisible)
        {
            if (isVisible){
                tvState.setVisibility(View.VISIBLE);
            }else{
                tvState.setVisibility(View.INVISIBLE);
            }
        }

        public void setIvImage(Context context, int resourceValue)
        {
            Glide.with(context)
                    .load(resourceValue)
                    .into(ivStory);
        }
        protected void setLikeBtn(String post_key) {
            FireBaseUtils.setLikeBtn(post_key,btnLiked);
        }
        private void setPostViewed(String post_key) {
            FireBaseUtils.setPostViewed(post_key,btnViews);
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

}
