package com.example.mlwithtensorflowlite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

import androidx.appcompat.app.AppCompatActivity;

public class BaseNavigationActivity extends AppCompatActivity {

    protected void setupBottomNavigation(int currentId) {
        ImageButton home = findViewById(R.id.navHome);
        ImageButton gallery = findViewById(R.id.navGallery);
        ImageButton tutorial = findViewById(R.id.tutorial);
        ImageButton info = findViewById(R.id.navInfo);

        home.setOnClickListener(v -> {
            if (currentId != R.id.navHome) {
                startActivity(new Intent(this, MainActivity.class));
                overridePendingTransition(0, 0); // No animation
                finish();
            }
        });

        gallery.setOnClickListener(v -> {
            if (currentId != R.id.navGallery) {
                startActivity(new Intent(this, GalleryActivity.class));
                overridePendingTransition(0, 0); // No animation
                finish();
            }
        });

        tutorial.setOnClickListener(v -> {
            if (currentId != R.id.tutorial) {
                startActivity(new Intent(this, TutorialActivity.class));
                overridePendingTransition(0, 0); // No animation
                finish();
            }
        });

        info.setOnClickListener(v -> {
            if (currentId != R.id.navInfo) {
                startActivity(new Intent(this, InfoActivity.class));
                overridePendingTransition(0, 0); // No animation
                finish();
            }
        });
    }
}
