package com.example.btl_nhom16;

import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {
    private EditText editTextUsername, editTextPassword;
    private DatabaseHelper databaseHelper;
    private Button btnDeleteData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        databaseHelper = new DatabaseHelper(this);

        btnDeleteData = findViewById(R.id.btnDeleteData); // Lấy đối tượng nút
        btnDeleteData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteDatabaseData(); // Gọi phương thức để xóa dữ liệu
            }
        });
    }

    public void loginUser(View view) {
        String username = editTextUsername.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        // Kiểm tra tên người dùng và mật khẩu
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

    // Phương thức để chuyển sang màn hình đăng ký
    public void goToRegister(View view) {
        Intent intent = new Intent(this, RegisterActivity.class);
        startActivity(intent);
    }

    // Phương thức xóa dữ liệu
    private void deleteDatabaseData() {
        // Xóa tất cả dữ liệu trong bảng tasks và users
        SQLiteDatabase db = databaseHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + DatabaseHelper.TABLE_TASKS);
        db.execSQL("DELETE FROM " + DatabaseHelper.TABLE_USERS);
        db.close();

        // Hiển thị thông báo
        Toast.makeText(this, "All data has been deleted.", Toast.LENGTH_SHORT).show();
    }

}
