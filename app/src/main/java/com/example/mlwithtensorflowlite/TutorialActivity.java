package com.example.mlwithtensorflowlite;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.callback.ErrorInfo;
import com.cloudinary.android.callback.UploadCallback;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TutorialActivity extends BaseNavigationActivity {

    private static final int PICK_IMAGE_REQUEST = 1001;

    EditText postInput;
    TextView postPrompt;
    Button postButton, attachImageButton;
    ImageView imagePreview;
    RecyclerView postFeedRecycler;

    FirebaseFirestore db;
    FirebaseAuth mAuth;

    Uri imageUri = null;
    String username = "User";

    List<QueryDocumentSnapshot> postList = new ArrayList<>();
    PostAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tutorial);

        setupBottomNavigation(R.id.tutorial);

        postInput = findViewById(R.id.postInput);
        postPrompt = findViewById(R.id.postPrompt);
        postButton = findViewById(R.id.postButton);
        attachImageButton = findViewById(R.id.attachImageButton);
        imagePreview = findViewById(R.id.imagePreview);
        postFeedRecycler = findViewById(R.id.postFeedRecycler);

        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();

        // Initialize Cloudinary safely
        try {
            MediaManager.get();
        } catch (IllegalStateException e) {
            Map<String, String> config = new HashMap<>();
            config.put("cloud_name", "dffjrfnn7");
            config.put("api_key", "754693833149612");
            config.put("api_secret", "YxztStUJrwO2BP_ALMtj7VfrdTo");
            MediaManager.init(this, config);
        }

        FirebaseUser user = mAuth.getCurrentUser();
        if (user != null) {
            db.collection("users").document(user.getUid()).get()
                    .addOnSuccessListener(documentSnapshot -> {
                        username = documentSnapshot.getString("username");
                        if (username == null || username.isEmpty()) username = "User";
                        postPrompt.setText("What's on your mind, " + username + "?");
                    });
        }

        adapter = new PostAdapter(this, postList);
        postFeedRecycler.setLayoutManager(new LinearLayoutManager(this));
        postFeedRecycler.setAdapter(adapter);

        loadPosts();

        attachImageButton.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent, PICK_IMAGE_REQUEST);
        });

        postButton.setOnClickListener(view -> {
            String content = postInput.getText().toString().trim();
            if (content.isEmpty()) {
                Toast.makeText(this, "Please enter a post", Toast.LENGTH_SHORT).show();
                return;
            }

            if (user == null) {
                Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show();
                return;
            }

            if (imageUri != null) {
                uploadImageToCloudinary(user.getUid(), content);
            } else {
                uploadPost(user.getUid(), content, null);
            }
        });
    }

    private void loadPosts() {
        db.collection("posts")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .addSnapshotListener((snapshots, error) -> {
                    if (error != null || snapshots == null) return;
                    postList.clear();
                    for (QueryDocumentSnapshot doc : snapshots) {
                        postList.add(doc);
                    }
                    adapter.notifyDataSetChanged();
                });
    }

    private void uploadImageToCloudinary(String uid, String content) {
        try {
            Bitmap bitmap = getBitmapFromUri(imageUri);
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, stream);
            byte[] imageBytes = stream.toByteArray();

            MediaManager.get().upload(imageBytes)
                    .callback(new UploadCallback() {
                        @Override
                        public void onStart(String requestId) {
                            Toast.makeText(TutorialActivity.this, "Uploading image...", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onProgress(String requestId, long bytes, long totalBytes) {}

                        @Override
                        public void onSuccess(String requestId, Map resultData) {
                            String imageUrl = resultData.get("secure_url").toString();
                            uploadPost(uid, content, imageUrl);
                        }

                        @Override
                        public void onError(String requestId, ErrorInfo error) {
                            Toast.makeText(TutorialActivity.this, "Upload failed: " + error.getDescription(), Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onReschedule(String requestId, ErrorInfo error) {}
                    }).dispatch();
        } catch (IOException e) {
            Toast.makeText(this, "Failed to get image data", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            return ImageDecoder.decodeBitmap(ImageDecoder.createSource(getContentResolver(), uri));
        } else {
            return MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);
        }
    }

    private void uploadPost(String uid, String content, @Nullable String imageUrl) {
        Map<String, Object> post = new HashMap<>();
        post.put("uid", uid);
        post.put("username", username);
        post.put("content", content);
        post.put("timestamp", com.google.firebase.Timestamp.now());
        if (imageUrl != null) post.put("imageUrl", imageUrl);

        db.collection("posts").add(post)
                .addOnSuccessListener(doc -> {
                    Toast.makeText(this, "Post shared!", Toast.LENGTH_SHORT).show();
                    postInput.setText("");
                    imagePreview.setImageDrawable(null);
                    imagePreview.setVisibility(ImageView.GONE);
                    imageUri = null;
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to post: " + e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null) {
            imageUri = data.getData();
            imagePreview.setImageURI(imageUri);
            imagePreview.setVisibility(ImageView.VISIBLE);
        }
    }
}
