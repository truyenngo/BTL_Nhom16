package com.example.btl_nhom16;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import androidx.core.app.NotificationCompat;

public class NotificationReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        // Tạo thông báo
        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, "dailyNotification")
                .setSmallIcon(R.drawable.ic_notification) // Icon thông báo
                .setContentTitle("Nhắc nhở công việc")
                .setContentText("Đã đến giờ bạn cần làm công việc quan trọng!")
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);

        // Hiển thị thông báo
        NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        // Tạo kênh thông báo (chỉ cần tạo một lần)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    "dailyNotification",
                    "Thông báo hàng ngày",
                    NotificationManager.IMPORTANCE_HIGH
            );
            notificationManager.createNotificationChannel(channel);
        }

        notificationManager.notify(1, builder.build());
    }
}
