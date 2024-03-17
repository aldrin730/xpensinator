package com.example.xpensinator;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

public class LoginActivity extends AppCompatActivity {

    EditText txtEmailLogin;
    EditText txtPasswordLogin;
    Button btnLogin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        DBHandler dbHandler = new DBHandler(this);

        txtEmailLogin = findViewById(R.id.txtEmailLogin);
        txtPasswordLogin = findViewById(R.id.txtPasswordLogin);
        btnLogin = findViewById(R.id.btnLogin);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get user input
                String email = txtEmailLogin.getText().toString().trim();
                String password = txtPasswordLogin.getText().toString().trim();

                // Check if any fields are empty
                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Validate user credentials
                boolean isValid = dbHandler.checkUser(email, password);
                if (isValid) {
                    // Login successful, redirect to dashboard activity
                    startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                    finish(); // Finish current activity
                } else {
                    // Invalid credentials, display error message
                    Toast.makeText(LoginActivity.this, "Invalid email or password", Toast.LENGTH_SHORT).show();
                }
            }
        });

    }
}