package com.example.mlwithtensorflowlite;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private final Context context;
    private List<QueryDocumentSnapshot> posts;
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    public PostAdapter(Context context, List<QueryDocumentSnapshot> posts) {
        this.context = context;
        this.posts = posts;
    }

    public static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView postUsername, postTimestamp, postContent, likeCount, commentCount;
        ImageView postImage, likeIcon, commentIcon;

        public PostViewHolder(View itemView) {
            super(itemView);
            postUsername = itemView.findViewById(R.id.postUsername);
            postTimestamp = itemView.findViewById(R.id.postTimestamp);
            postContent = itemView.findViewById(R.id.postContent);
            postImage = itemView.findViewById(R.id.postImage);
            likeIcon = itemView.findViewById(R.id.likeIcon);
            commentIcon = itemView.findViewById(R.id.commentIcon);
            likeCount = itemView.findViewById(R.id.likeCount);
            commentCount = itemView.findViewById(R.id.commentCount);
        }
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        QueryDocumentSnapshot post = posts.get(position);
        String postId = post.getId();
        String username = post.getString("username");
        String content = post.getString("content");

        holder.postUsername.setText(username);
        holder.postContent.setText(content);

        // Timestamp formatting
        Timestamp timestampObj = post.getTimestamp("timestamp");
        if (timestampObj != null) {
            Date date = timestampObj.toDate();
            String formattedTime = new SimpleDateFormat("MMM dd, yyyy hh:mm a", Locale.getDefault()).format(date);
            holder.postTimestamp.setText(formattedTime);
        } else {
            holder.postTimestamp.setText("Unknown time");
        }

        // Load image
        String imageUrl = post.getString("imageUrl");
        if (imageUrl != null && !imageUrl.isEmpty()) {
            holder.postImage.setVisibility(View.VISIBLE);
            Glide.with(context).load(imageUrl).centerCrop().into(holder.postImage);
        } else {
            holder.postImage.setVisibility(View.GONE);
        }

        // Initial like/comment count
        Long likes = post.getLong("likeCount");
        Long comments = post.getLong("commentCount");
        holder.likeCount.setText(String.valueOf(likes != null ? likes : 0));
        holder.commentCount.setText(String.valueOf(comments != null ? comments : 0));

        // Toggle Like Count Only
        String userId = mAuth.getCurrentUser().getUid();
        DocumentReference likeRef = db.collection("posts").document(postId)
                .collection("likes").document(userId);
        DocumentReference postRef = db.collection("posts").document(postId);

        holder.likeIcon.setOnClickListener(v -> {
            likeRef.get().addOnSuccessListener(snapshot -> {
                if (snapshot.exists()) {
                    // Unlike
                    likeRef.delete();
                    postRef.update("likeCount", com.google.firebase.firestore.FieldValue.increment(-1));
                    int currentCount = Integer.parseInt(holder.likeCount.getText().toString());
                    holder.likeCount.setText(String.valueOf(Math.max(currentCount - 1, 0)));
                } else {
                    // Like
                    Map<String, Object> likeData = new HashMap<>();
                    likeData.put("timestamp", com.google.firebase.firestore.FieldValue.serverTimestamp());
                    likeRef.set(likeData);
                    postRef.update("likeCount", com.google.firebase.firestore.FieldValue.increment(1));
                    int currentCount = Integer.parseInt(holder.likeCount.getText().toString());
                    holder.likeCount.setText(String.valueOf(currentCount + 1));
                }
            }).addOnFailureListener(e -> {
                Toast.makeText(context, "Error toggling like", Toast.LENGTH_SHORT).show();
            });
        });

        // Comment intent
        holder.commentIcon.setOnClickListener(v -> {
            Intent intent = new Intent(context, CommentsActivity.class);
            intent.putExtra("postId", postId);
            intent.putExtra("username", username);
            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return posts != null ? posts.size() : 0;
    }

    public void updatePosts(List<QueryDocumentSnapshot> newPosts) {
        this.posts = newPosts;
        notifyDataSetChanged();
    }
}
