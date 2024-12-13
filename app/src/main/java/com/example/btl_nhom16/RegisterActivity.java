package com.example.btl_nhom16;

import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {
    private EditText editTextUsername, editTextPassword, editTextConfirmPassword;
    private DatabaseHelper databaseHelper;
    TextView textViewExit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        textViewExit = findViewById(R.id.textViewExit);
        textViewExit.setPaintFlags(textViewExit.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        editTextConfirmPassword = findViewById(R.id.editTextConfirmPassword);
        databaseHelper = new DatabaseHelper(this);
    }

    public void registerUser(View view) {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();
        String confirmPassword = editTextConfirmPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.equals(confirmPassword)) {
            if (databaseHelper.checkUser(username, password)) {
                Toast.makeText(this, "User already exists", Toast.LENGTH_SHORT).show();
            } else {
                if (databaseHelper.addUser(username, password)) {
                    Toast.makeText(this, "User registered successfully", Toast.LENGTH_SHORT).show();
                    finish();
                } else {
                    Toast.makeText(this, "Registration failed", Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show();
        }
    }

    public void exitToLogin(View view) {
        finish();
    }
}


