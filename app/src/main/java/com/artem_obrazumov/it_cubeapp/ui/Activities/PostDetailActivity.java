package com.artem_obrazumov.it_cubeapp.ui.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.artem_obrazumov.it_cubeapp.Adapters.GalleryAdapter;
import com.artem_obrazumov.it_cubeapp.Models.ContestPostModel;
import com.artem_obrazumov.it_cubeapp.Models.PostModel;
import com.artem_obrazumov.it_cubeapp.Models.UserModel;
import com.artem_obrazumov.it_cubeapp.R;
import com.artem_obrazumov.it_cubeapp.databinding.ActivityPostDetailBinding;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

// Активность для детального просмотра постов (всех типов)
public class PostDetailActivity extends AppCompatActivity {

    // Данные поста
    int postType;
    private String postID = "";
    boolean isEditable = false;
    boolean isUserAdmin = false;

    // Binding
    private ActivityPostDetailBinding binding;

    // Интерфейс для нажатий на изображения
    private GalleryAdapter.OnImageClickListener onImageClickListener;

    // Адаптер для изображений
    private GalleryAdapter imagesAdapter;

    // Верхнее меню
    private Menu activityMenu;

    // Firebase
    private FirebaseAuth auth;

    // Окно для уведомления пользователя загрузке
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityPostDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Получаем тип поста и его идентификатор
        postType = getIntent().getIntExtra("PostType", PostModel.POST_TYPE_NEWS);
        if (postType == PostModel.POST_TYPE_NEWS) {
            LinearLayout contest_extras = findViewById(R.id.contest_extras);
            contest_extras.setVisibility(View.GONE);
        }
        postID = getIntent().getStringExtra("PostID");

        // Инициализация элементов
        progressDialog = new ProgressDialog(this);
        auth = FirebaseAuth.getInstance();
        imagesAdapter = new GalleryAdapter(GalleryAdapter.MODE_CROPPED);
    }

    @Override
    protected void onResume() {
        checkIfUserIsAdmin(auth.getCurrentUser().getUid());
        getPostData(postID, postType);
        initializeImagesAdapter();
        super.onResume();
    }

    // Проверяем, является ли пользователь администратором
    private void checkIfUserIsAdmin(String uid) {
        UserModel.getUserQuery(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds : snapshot.getChildren()) {
                    UserModel user = ds.getValue(UserModel.class);
                    if (user.getUserStatus() >= UserModel.STATUS_ADMIN) {
                        isUserAdmin = true;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Если не удалось получить данные, то оставляем значение по умолчанию
            }
        });
    }

    private void getPostData(String postID, int postType) {
        if (postType == PostModel.POST_TYPE_NEWS) {
            PostModel.getNewsPostQuery(postID).addValueEventListener(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                // Получение данных и заполнение полей
                                PostModel post = ds.getValue(PostModel.class);
                                binding.postTitle.setText(post.getTitle());
                                binding.postContent.setText(post.getDescription());

                                Calendar calendar = Calendar.getInstance(Locale.getDefault());
                                calendar.setTimeInMillis(post.getUploadTime());
                                String time = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(calendar.getTime());
                                binding.requestDate.setText(time);
                                setActionBarTitle(post.getTitle());

                                getUserData(post.getAuthorUid());

                                // Загружаем массив с изображениями в адаптер
                                imagesAdapter.setDataSet(post.getImagesURLs());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Получение поста не удалось, уведомляем пользователя об ошибке
                            Toast.makeText(getApplicationContext(), R.string.failed_loading_posts, Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        } else if (postType == PostModel.POST_TYPE_HACKATHON) {
            PostModel.getHackathonPostQuery(postID).addValueEventListener(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                // Получение данных и заполнение полей
                                ContestPostModel post = ds.getValue(ContestPostModel.class);
                                binding.postTitle.setText(post.getTitle());
                                binding.postContent.setText(post.getDescription());

                                Calendar calendar = Calendar.getInstance(Locale.getDefault());
                                calendar.setTimeInMillis(post.getUploadTime());
                                String time = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(calendar.getTime());
                                checkIfOver(post);
                                binding.requestDate.setText(time);
                                setActionBarTitle(post.getTitle());

                                getUserData(post.getAuthorUid());

                                // Загружаем массив с изображениями в адаптер
                                imagesAdapter.setDataSet(post.getImagesURLs());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Получение поста не удалось, уведомляем пользователя об ошибке
                            Toast.makeText(getApplicationContext(), R.string.failed_loading_posts, Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        } else {
            PostModel.getContestPostQuery(postID).addValueEventListener(
                    new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            for (DataSnapshot ds : snapshot.getChildren()) {
                                // Получение данных и заполнение полей
                                ContestPostModel post = ds.getValue(ContestPostModel.class);
                                binding.postTitle.setText(post.getTitle());
                                binding.postContent.setText(post.getDescription());

                                Calendar calendar = Calendar.getInstance(Locale.getDefault());
                                calendar.setTimeInMillis(post.getUploadTime());
                                String time = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(calendar.getTime());
                                checkIfOver(post);
                                binding.requestDate.setText(time);
                                setActionBarTitle(post.getTitle());

                                getUserData(post.getAuthorUid());

                                // Загружаем массив с изображениями в адаптер
                                imagesAdapter.setDataSet(post.getImagesURLs());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            // Получение поста не удалось, уведомляем пользователя об ошибке
                            Toast.makeText(getApplicationContext(), R.string.failed_loading_posts, Toast.LENGTH_SHORT).show();
                        }
                    }
            );
        }
    }

    // Проверяен, можно ли еще записаться на мероприятие
    private void checkIfOver(ContestPostModel post) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if (currentTime > post.getOpenUntil()) {
            binding.isOpened.setText(getString(R.string.contestIsClosed));
            binding.isOpened.setTextColor(Color.rgb(255, 0, 0));
            binding.participateButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(getApplicationContext(), getString(R.string.contestIsClosed),
                            Toast.LENGTH_SHORT).show();
                }
            });
            binding.participateButton.setAlpha(0.5f);
        }
    }

    // Получение информации об авторе и заполнение полей
    private void getUserData(String authorUid) {
        UserModel.getUserQuery(authorUid).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            UserModel author = ds.getValue(UserModel.class);
                            binding.postAuthorName.setText( author.getName() + " " + author.getSurname() );

                            String avatarURL = author.getAvatar();
                            try {
                                Glide.with(PostDetailActivity.this).load(avatarURL).placeholder(R.drawable.default_user_profile_icon).into(binding.postAuthorAvatar);
                            } catch (Exception e) {
                                // Если не удалось загрузить изображение с интернета, ничего не делаем
                            }

                            // Даем доступ к редактированию поста, только если пользователь является автором или администратором
                            isEditable = (authorUid.equals(auth.getCurrentUser().getUid()) || isUserAdmin);
                            if (isEditable) {
                                try {
                                    activityMenu.clear();
                                    getMenuInflater().inflate(R.menu.post_author_menu, activityMenu);
                                    // Пытаемся удалить опцию просмотра поста
                                    activityMenu.removeItem(R.id.view_post_option);
                                } catch (Exception e) {
                                    // Если произошла ошибка, то ничего не делаем
                                }
                            }

                            progressDialog.dismiss();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                }
        );
    }

    // Инициализация адаптера для изображений
    private void initializeImagesAdapter() {
        binding.imagesRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.imagesRecyclerView.setNestedScrollingEnabled(false);
        binding.imagesRecyclerView.setAdapter(imagesAdapter);
        initializeOnImageClickListener();
    }

    // Установка обработчика нажатий на изображения
    private void initializeOnImageClickListener() {
        onImageClickListener = new GalleryAdapter.OnImageClickListener() {
            @Override
            public void onClick(View v, String url) {
                // Переходим на другую активность и передаем ссылку на фото
                Intent intent = new Intent(getApplicationContext(), ImageViewerActivity.class);
                intent.putExtra("imageURL", url);
                startActivity(intent);
            }
        };

        imagesAdapter.setOnImageClickListener(onImageClickListener);
    }

    // Установка другого текста на верхней панели
    private void setActionBarTitle(String title) {
        try {
            getActionBar().setTitle(title);
            getActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            // При возникновении ошибки используем другой способ
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    // Инициализация объекта меню для последующего использования
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        activityMenu = menu;
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.edit_post_option:
                // Кнопка "редактировать"
                Intent intent = new Intent(this, NewPostActivity.class);
                intent.putExtra("mode", NewPostActivity.EDIT_POST);
                intent.putExtra("existingPostID", postID);
                intent.putExtra("postType", postType);
                startActivity(intent);
                break;
            case R.id.delete_post_option:
                // Кнопка "удалить"
                new AlertDialog.Builder(this)
                        .setMessage( getString(R.string.want_to_delete) )
                        .setCancelable(true)
                        .setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // Удаление поста
                                ErasePost(postID);
                            }
                        })
                        .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                            }
                        }).create().show();
                break;
            default:
                // Кнопка "назад"
                onBackPressed();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    // Метод для удаления поста
    private void ErasePost(String postID) {
        // Получаем ссылку на пост
        DatabaseReference reference;
        if (postType == PostModel.POST_TYPE_NEWS) {
            reference = FirebaseDatabase.getInstance().getReference("News_posts")
                    .child(postID);
        }
        else if (postType == PostModel.POST_TYPE_HACKATHON) {
            reference = FirebaseDatabase.getInstance().getReference("Hackathon_posts")
                    .child(postID);
        } else {
            reference = FirebaseDatabase.getInstance().getReference("Contest_posts")
                    .child(postID);
        }

        // Удаляем пост
        reference.removeValue().addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Удаление поста завершено
                        onBackPressed();
                    }
                }
        ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(getApplicationContext(), getString(R.string.unable_to_delete_post), Toast.LENGTH_SHORT).show();
            }
        });
    }
}