package com.example.mlwithtensorflowlite;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import androidx.annotation.Nullable;

public class InfoActivity extends BaseNavigationActivity {

    private LinearLayout historyContainer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_info);
        setupBottomNavigation(R.id.navInfo);

        historyContainer = findViewById(R.id.historyContainer);

        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) {
            Toast.makeText(this, "You must be logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .document(currentUser.getUid())
                .collection("history")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    LayoutInflater inflater = LayoutInflater.from(this);
                    int shownCount = 0;

                    for (QueryDocumentSnapshot doc : queryDocumentSnapshots) {
                        String imageUri = doc.getString("imageUri");
                        String label = doc.getString("label");
                        String doSummary = doc.getString("doSummary");
                        String dontSummary = doc.getString("dontSummary");
                        String proTip = doc.getString("proTip");

                        // Skip entries with missing AI tips
                        if (doSummary == null || dontSummary == null || proTip == null) continue;

                        View itemView = inflater.inflate(R.layout.item_history_card, historyContainer, false);

                        ImageView thumbnail = itemView.findViewById(R.id.historyImage);
                        TextView labelText = itemView.findViewById(R.id.historyLabel);
                        TextView doText = itemView.findViewById(R.id.historyDo);
                        TextView dontText = itemView.findViewById(R.id.historyDont);
                        TextView tipText = itemView.findViewById(R.id.historyTip);
                        LinearLayout expanded = itemView.findViewById(R.id.expandedContent);

                        // Set values
                        if (imageUri != null) thumbnail.setImageURI(Uri.parse(imageUri));
                        labelText.setText(label != null ? label : "Unknown");
                        doText.setText(doSummary);
                        dontText.setText(dontSummary);
                        tipText.setText(proTip);

                        // Expand/collapse
                        itemView.setOnClickListener(v -> {
                            expanded.setVisibility(
                                    expanded.getVisibility() == View.VISIBLE ? View.GONE : View.VISIBLE
                            );
                        });

                        historyContainer.addView(itemView);
                        shownCount++;
                    }

                    if (shownCount == 0) {
                        Toast.makeText(this, "No valid classification history yet.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Failed to load history", Toast.LENGTH_SHORT).show();
                });
    }
}
