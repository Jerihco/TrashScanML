package com.example.mlwithtensorflowlite;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.Legend;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class GalleryActivity extends BaseNavigationActivity {

    private TextView incomeAmount, expenseAmount;
    private TextView usernameTextView, emailTextView;
    private BarChart barChart;
    private Button logoutButton;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gallery);

        setupBottomNavigation(R.id.navGallery);

        incomeAmount = findViewById(R.id.incomeAmount);
        expenseAmount = findViewById(R.id.expenseAmount);
        usernameTextView = findViewById(R.id.usernameTextView);
        emailTextView = findViewById(R.id.emailTextView);
        barChart = findViewById(R.id.barChart);
        logoutButton = findViewById(R.id.logoutButton); // Button in your XML layout

        logoutButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Intent intent = new Intent(GalleryActivity.this, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            }
        });

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            String email = user.getEmail();
            String displayName = user.getDisplayName();

            if (email != null) {
                emailTextView.setText(email);
            }

            FirebaseFirestore.getInstance().collection("users")
                    .document(uid)
                    .get()
                    .addOnSuccessListener(snapshot -> {
                        if (snapshot.exists()) {
                            if (displayName != null && !displayName.isEmpty()) {
                                usernameTextView.setText(displayName);
                            } else {
                                String username = snapshot.getString("username");
                                if (username != null) {
                                    usernameTextView.setText(username);
                                }
                            }
                            updateStats(snapshot);
                        }
                    });
        }
    }

    private void updateStats(DocumentSnapshot snapshot) {
        List<String> labels = Arrays.asList("cardboard", "glass", "metal", "paper", "plastic");
        List<BarEntry> entries = new ArrayList<>();

        int total = 0;
        for (int i = 0; i < labels.size(); i++) {
            Long value = snapshot.getLong(labels.get(i));
            int count = value != null ? value.intValue() : 0;
            total += count;
            entries.add(new BarEntry(i, count));
        }

        List<?> usedDates = (List<?>) snapshot.get("usedDates");
        int usedDays = usedDates != null ? usedDates.size() : 0;

        incomeAmount.setText(usedDays + " days");
        expenseAmount.setText(total + " items");

        BarDataSet dataSet = new BarDataSet(entries, "Trash Types");
        dataSet.setColors(
                ContextCompat.getColor(this, R.color.brown),
                ContextCompat.getColor(this, R.color.glassy_cyan),
                ContextCompat.getColor(this, R.color.gray),
                ContextCompat.getColor(this, R.color.egg_white),
                ContextCompat.getColor(this, R.color.blue)
        );

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);
        barData.setValueTextColor(Color.BLACK);
        barData.setValueTextSize(12f);

        barChart.setData(barData);
        barChart.setFitBars(true);
        barChart.setDrawGridBackground(false);
        barChart.setBackgroundColor(Color.WHITE);
        barChart.getDescription().setEnabled(false);
        barChart.setDrawValueAboveBar(true);

        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setTextColor(Color.BLACK);
        xAxis.setTextSize(12f);
        xAxis.setValueFormatter(new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                int index = (int) value;
                return (index >= 0 && index < labels.size()) ? labels.get(index) : "";
            }
        });

        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setAxisMinimum(0f);
        leftAxis.setTextColor(Color.BLACK);
        leftAxis.setGridColor(Color.LTGRAY);
        barChart.getAxisRight().setEnabled(false);

        Legend legend = barChart.getLegend();
        legend.setTextColor(Color.BLACK);
        legend.setTextSize(12f);

        barChart.invalidate(); // Refresh chart
    }
}
