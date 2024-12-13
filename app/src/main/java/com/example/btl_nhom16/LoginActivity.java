package com.example.btl_nhom16;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText editTextUsername, editTextPassword;
    private DatabaseHelper databaseHelper;
    private Button btnDeleteData;
    TextView textViewRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        textViewRegister = findViewById(R.id.textViewRegister);
        textViewRegister.setPaintFlags(textViewRegister.getPaintFlags() | Paint.UNDERLINE_TEXT_FLAG);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        databaseHelper = new DatabaseHelper(this);

        btnDeleteData = findViewById(R.id.btnDeleteData);
        btnDeleteData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDatabaseData();
            }
        });
    }

    public void loginUser(View view) {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        Log.d("LoginActivity", "Username: " + username);
        Log.d("LoginActivity", "Password: " + password);

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Please enter both username and password", Toast.LENGTH_SHORT).show();
        } else {
            if (databaseHelper.checkUser(username, password)) {
                Toast.makeText(this, "Login successful", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(this, HomeActivity.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Invalid username or password", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void goToRegister(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    private void deleteDatabaseData() {
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + DatabaseHelper.TABLE_TASKS);
        db.execSQL("DELETE FROM " + DatabaseHelper.TABLE_USERS);
        db.close();
        Toast.makeText(this, "All data has been deleted.", Toast.LENGTH_SHORT).show();
    }

}
