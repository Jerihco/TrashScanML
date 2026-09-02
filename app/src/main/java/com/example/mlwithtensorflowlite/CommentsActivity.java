package com.example.mlwithtensorflowlite;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class CommentsActivity extends AppCompatActivity {

    private RecyclerView commentsRecyclerView;
    private EditText commentInput;
    private Button sendButton;

    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private CommentAdapter commentAdapter;
    private ArrayList<Comment> commentList;

    private String postId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comments);

        commentsRecyclerView = findViewById(R.id.commentsRecyclerView);
        commentInput = findViewById(R.id.commentInput);
        sendButton = findViewById(R.id.sendCommentButton);

        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();

        postId = getIntent().getStringExtra("postId");
        if (postId == null) {
            Toast.makeText(this, "Post ID missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        commentList = new ArrayList<>();
        commentAdapter = new CommentAdapter(this, commentList);
        commentsRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        commentsRecyclerView.setAdapter(commentAdapter);

        loadComments();

        sendButton.setOnClickListener(view -> {
            String commentText = commentInput.getText().toString().trim();
            if (commentText.isEmpty()) {
                Toast.makeText(this, "Comment cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            postComment(commentText);
        });
    }

    private void loadComments() {
        CollectionReference commentsRef = db.collection("posts")
                .document(postId)
                .collection("comments");

        commentsRef.orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e("CommentsActivity", "Error fetching comments", e);
                        Toast.makeText(this, "Error loading comments.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (snapshots != null) {
                        commentList.clear();
                        for (DocumentChange dc : snapshots.getDocumentChanges()) {
                            Comment comment = dc.getDocument().toObject(Comment.class);
                            commentList.add(comment);
                        }
                        commentAdapter.notifyDataSetChanged();
                    }
                });
    }

    private void postComment(String content) {
        String userId = auth.getCurrentUser().getUid();
        String username = auth.getCurrentUser().getDisplayName();  // Optional: fallback to custom displayName field

        Map<String, Object> comment = new HashMap<>();
        comment.put("userId", userId);
        comment.put("username", username != null ? username : "Anonymous");
        comment.put("content", content);
        comment.put("timestamp", Timestamp.now());

        db.collection("posts")
                .document(postId)
                .collection("comments")
                .add(comment)
                .addOnSuccessListener(documentReference -> {
                    commentInput.setText("");
                    Toast.makeText(this, "Comment posted", Toast.LENGTH_SHORT).show();
                    incrementCommentCount();
                })
                .addOnFailureListener(e -> {
                    Log.e("CommentsActivity", "Failed to post comment", e);
                    Toast.makeText(this, "Failed to post comment", Toast.LENGTH_SHORT).show();
                });
    }

    private void incrementCommentCount() {
        db.collection("posts")
                .document(postId)
                .update("commentCount", com.google.firebase.firestore.FieldValue.increment(1));
    }
}
