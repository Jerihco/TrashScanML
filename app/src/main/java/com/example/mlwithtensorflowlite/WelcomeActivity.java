package com.example.mlwithtensorflowlite;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

public class WelcomeActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private Button getStartedButton;

    private final int[] tutorialImages = {
            R.drawable.tutorial1, // page 1
            R.drawable.tutorial2, // page 2
            R.drawable.tutorial3,  // page 3
            R.drawable.tutorial4
    };

    private final String[] tutorialTexts = {
            "Swipe right to continue", // no caption for page 1
            "Tap the use camera button", // page 2
            "Take a picture", // page 3
            "Get results"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        viewPager = findViewById(R.id.tutorialViewPager);
        getStartedButton = findViewById(R.id.buttonGetStarted);

        TutorialAdapter adapter = new TutorialAdapter(tutorialImages, tutorialTexts);
        viewPager.setAdapter(adapter);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                if (position == tutorialImages.length - 1) {
                    getStartedButton.setVisibility(View.VISIBLE);
                } else {
                    getStartedButton.setVisibility(View.INVISIBLE);
                }
            }
        });

        getStartedButton.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, MainActivity.class));
            finish();
        });
    }
}
