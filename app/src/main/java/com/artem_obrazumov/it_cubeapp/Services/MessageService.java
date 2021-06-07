package com.artem_obrazumov.it_cubeapp.Services;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;

import com.artem_obrazumov.it_cubeapp.ui.Activities.MainActivity;
import com.artem_obrazumov.it_cubeapp.ui.Activities.MyRequestsActivity;
import com.artem_obrazumov.it_cubeapp.ui.Activities.PostDetailActivity;
import com.artem_obrazumov.it_cubeapp.Models.PostModel;
import com.artem_obrazumov.it_cubeapp.Models.UserModel;
import com.artem_obrazumov.it_cubeapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;

import java.util.UUID;

public class MessageService extends FirebaseMessagingService {
    /*
        Типы уведомлений:
        1 - уведомление о новой новости
        2 - уведомление о новом хакатоне
        3 - уведомление о новом конкурсе

        11 - уведомление о зачислении
        12 - уведомление об отклонении
        13 - уведомление о попадании в резерв

        Уведомления для администраторов
        21 - уведомление о появлении новой заявки
     */

    public static final int NEW_NEWS_POST_MESSAGE = PostModel.POST_TYPE_NEWS;
    public static final int NEW_HACKATHON_POST_MESSAGE = PostModel.POST_TYPE_HACKATHON;
    public static final int NEW_CONTEST_POST_MESSAGE = PostModel.POST_TYPE_CONTEST;

    public static final int REQUEST_APPROVED = 11;
    public static final int REQUEST_DENIED = 12;
    public static final int REQUEST_RESERVED = 13;
    public static final int REQUEST_ENROLLED = 14;

    public static final int NEW_REQUEST_NOTIFICATION = 21;

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);
        // Обработка полученных сообщений
        try {
            if (isMessageAvailable(remoteMessage)) {
                Intent intent;
                int messageType = Integer.parseInt(remoteMessage.getData().get("messageType"));

                if (messageType <= 10) {
                    // Типы сообщений 1-10 - посты различных типов
                    String PostID = remoteMessage.getData().get("PostID");
                    intent = new Intent(this, PostDetailActivity.class);
                    intent.putExtra("PostID", PostID);
                    intent.putExtra("PostType", messageType);
                } else if (messageType >= 11 && messageType <= 20) {
                    // Типы сообщений 11-20 - посты откликов на заявки
                    intent = new Intent(this, MyRequestsActivity.class);
                } else {
                    // Не подходит не под один тип, отправляем на главный экран
                    intent = new Intent(this, MainActivity.class);
                }
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                createNotification(intent, remoteMessage, messageType);
            }
        } catch (Exception ignored) {}
    }

    // Проверяем, нужно ли отображать данное уведомление
    private boolean isMessageAvailable(RemoteMessage remoteMessage) throws Exception {
        int messageType = Integer.parseInt(remoteMessage.getData().get("messageType"));

        if (messageType >= 0 && messageType <= 10) {
            String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (remoteMessage.getData().get("postAuthorID").equals(currentUserID)) {
                return true;
                // TODO: Запретить автору принимать уведомления о своих постах
            }
        }

        if (messageType >= 11 && messageType <= 20) {
            // Даем просматривать уведомление только определенному пользователю
            String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            if (!remoteMessage.getData().get("toUser").equals(currentUserID)) {
                return false;
            }
        }

        if (messageType >= 21 && messageType <= 30) {
            // Уведомления для администраторов даем просматривать только администраторам
            String currentUserID = FirebaseAuth.getInstance().getCurrentUser().getUid();
            UserModel.getUserQuery(currentUserID).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for (DataSnapshot ds: snapshot.getChildren()) {
                        int userStatus = ds.getValue(UserModel.class).getUserStatus();
                        if (userStatus >= UserModel.STATUS_ADMIN) {
                            Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                            int messageType = Integer.parseInt(remoteMessage.getData().get("messageType"));
                            try {
                                createNotification(intent, remoteMessage, messageType);
                            } catch (Exception ignored) {}
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError ignored) {}
            });

            /*
                Так как для получения статуса пользователя нам может понадобиться неопределенное
                количество времени, мы возвращаем false и показываем уведомления администраторам
                только после получения результата
             */

            return false;
        }

        return true;
    }

    // Метод для создания уведомления
    private void createNotification(Intent intent, RemoteMessage message, int messageType) throws Exception {
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        String channelId = UUID.randomUUID().toString();
        NotificationCompat.Builder builder = new  NotificationCompat.Builder(this, channelId)
                .setSmallIcon(R.drawable.logo)
                .setContentTitle(getMessageTitle(messageType))
                .setContentText(message.getData().get("title")).setAutoCancel(true).setContentIntent(pendingIntent);;
        NotificationManager manager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, channelId, NotificationManager.IMPORTANCE_DEFAULT);
            manager.createNotificationChannel(channel);
        }
        manager.notify(0, builder.build());
    }

    // Подбор названия в зависимости от типа сообщения
    private String getMessageTitle(int messageType) {
        Context context = getApplicationContext();
        switch (messageType) {
            case NEW_HACKATHON_POST_MESSAGE:
                return context.getString(R.string.hackathon_notification);
            case NEW_CONTEST_POST_MESSAGE:
                return context.getString(R.string.contest_notification);
            case REQUEST_APPROVED:
                return context.getString(R.string.request_approved);
            case REQUEST_DENIED:
                return context.getString(R.string.request_denied);
            case REQUEST_RESERVED:
                return context.getString(R.string.request_reserved);
            case REQUEST_ENROLLED:
                return context.getString(R.string.request_enrolled);
            case NEW_REQUEST_NOTIFICATION:
                return context.getString(R.string.new_request);
            default:
                return context.getString(R.string.news_notification);
        }
    }
}
