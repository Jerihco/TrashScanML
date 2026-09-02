package com.example.mlwithtensorflowlite;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    EditText usernameInput, passwordInput;
    Button loginButton;
    TextView statusText;
    FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Initialize views
        usernameInput = findViewById(R.id.username);
        passwordInput = findViewById(R.id.password);
        loginButton = findViewById(R.id.loginButton);
        statusText = findViewById(R.id.statusText);

        // Firebase Auth instance
        mAuth = FirebaseAuth.getInstance();

        // Login button logic
        loginButton.setOnClickListener(v -> {
            String email = usernameInput.getText().toString().trim();
            String password = passwordInput.getText().toString().trim();

            // Input validation
            if (email.isEmpty() && password.isEmpty()) {
                statusText.setText("Please enter your email and password.");
                return;
            } else if (email.isEmpty()) {
                statusText.setText("Email is required.");
                return;
            } else if (password.isEmpty()) {
                statusText.setText("Password is required.");
                return;
            }

            // Firebase sign-in
            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            FirebaseUser user = mAuth.getCurrentUser();
                            Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, WelcomeActivity.class));
                            finish();
                        } else {
                            Exception e = task.getException();
                            String errorMessage = "Authentication failed.";

                            if (e != null && e.getMessage() != null) {
                                String msg = e.getMessage().toLowerCase();

                                if (msg.contains("no user record")) {
                                    errorMessage = "No account found with that email.";
                                } else if (msg.contains("password is invalid") || msg.contains("auth credential is incorrect")) {
                                    errorMessage = "Incorrect password. Please try again.";
                                } else if (msg.contains("email address is badly formatted")) {
                                    errorMessage = "Invalid email format.";
                                } else {
                                    errorMessage = "Login failed: " + e.getMessage();
                                }
                            }

                            statusText.setText(errorMessage);
                            Toast.makeText(LoginActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                        }
                    });
        });

        // Register button
        Button registerButton = findViewById(R.id.registerButton);
        registerButton.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }
}
