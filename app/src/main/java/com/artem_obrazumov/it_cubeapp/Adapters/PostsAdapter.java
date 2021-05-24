package com.artem_obrazumov.it_cubeapp.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.artem_obrazumov.it_cubeapp.Models.PostModel;
import com.artem_obrazumov.it_cubeapp.Models.UserModel;
import com.artem_obrazumov.it_cubeapp.R;
import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Locale;

public class PostsAdapter extends RecyclerView.Adapter<PostsAdapter.ViewHolder> {

    private int currentPosition = 0;

    // Константы
    // Значения зависят от значений в PostModel
    private static final int POST_TYPE_NEWS = PostModel.POST_TYPE_NEWS;
    private static final int POST_TYPE_CONTEST = PostModel.POST_TYPE_CONTEST;

    // Информация в адаптере
    private ArrayList<PostModel> dataSet;
    // Характеристики пользователя
    private UserModel userStats;

    //Интерфейсы для обработки нажатий
    private PostOnClickListener postOnClickListener;

    // Конструкторы для адаптера
    public PostsAdapter() {}
    public PostsAdapter(ArrayList<PostModel> dataSet) {
        this.dataSet = dataSet;
    }

    // Метод для обновления списка новостей
    public void setPostsDataSet(ArrayList<PostModel> newDataSet) {
        this.dataSet = newDataSet;
        notifyDataSetChanged();
    }

    // Метод для обновления характеристик пользователя
    public void setUserStats(UserModel userStats) {
        this.userStats = userStats;
    }

    // Класс ViewHolder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        // View-элементы
        private final ImageView post_author_avatar;
        private final TextView post_author_name, post_date, post_title, post_content;
        private final TextView post_options;
        private final RecyclerView images;

        // Адаптер для отображения картинок
        private GalleryAdapter adapter = new GalleryAdapter(GalleryAdapter.MODE_CROPPED);

        // Мини-меню для каждого поста
        private final PopupMenu menu;

        // Пост, с которым мы будем работать
        private String postID = "";

        public ViewHolder(View view) {
            super(view);
            view.setOnClickListener(this);

            // Инициализация View-элементов
            post_author_avatar = view.findViewById(R.id.post_author_avatar);
            post_author_name = view.findViewById(R.id.post_author_name);
            post_date = view.findViewById(R.id.request_date);
            post_title = view.findViewById(R.id.post_title);
            post_content = view.findViewById(R.id.post_content);
            post_options = view.findViewById(R.id.post_options);
            images = view.findViewById(R.id.images);

            // Инициализация меню
            Context context = post_author_avatar.getContext();
            menu = new PopupMenu(context, post_options);

            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.edit_post_option:
                            // Редактирование поста
                            postOnClickListener.onUpdatePost(postID, PostModel.POST_TYPE_NEWS);
                            break;
                        case R.id.delete_post_option:
                            // Удаление поста
                            new AlertDialog.Builder(context)
                                    .setMessage( context.getString(R.string.want_to_delete) )
                                    .setCancelable(true)
                                    .setPositiveButton("Ок", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            // Удаление поста
                                            ErasePost(postID, context);
                                        }
                                    })
                                    .setNegativeButton("Отмена", new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                        }
                                    }).create().show();
                            break;
                        default:
                            // Просмотр поста
                            onClick(view);
                            break;
                    }
                    return false;
                }
            });

            post_author_avatar.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String post_id = dataSet.get(getAdapterPosition()).getAuthorUid();
                    postOnClickListener.onAvatarClick(post_id);
                }
            });
        }

        public ImageView getPostAuthorAvatar() {
            return post_author_avatar;
        }

        public TextView getPostAuthorName() {
            return post_author_name;
        }

        public TextView getPostDate() {
            return post_date;
        }

        public TextView getPostTitle() {
            return post_title;
        }

        public TextView getPostContent() {
            return post_content;
        }

        public TextView getPostOptions() {
            return post_options;
        }

        public RecyclerView getImages() {
            return images;
        }

        public GalleryAdapter getAdapter() {
            return adapter;
        }

        public PopupMenu getMenu() {
            return menu;
        }

        public String getPostID() {
            return this.postID;
        }

        // Сеттеры
        public void setPostID(String uid) {
            this.postID = uid;
        }

        public void setAdapter(GalleryAdapter adapter) {
            this.adapter = adapter;
        }

        // Реализация интерфейса
        @Override
        public void onClick(View v) {
            String post_id = dataSet.get(getAdapterPosition()).getUid();
            postOnClickListener.onClick(post_id, PostModel.POST_TYPE_NEWS);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.news_post_row, parent, false);

        currentPosition++;
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PostModel currentPost = dataSet.get(position);
        UserModel.getUserQuery(currentPost.getAuthorUid()).addValueEventListener(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            UserModel author = ds.getValue(UserModel.class);
                            holder.getPostAuthorName().setText( author.getName() + " " + author.getSurname() );

                            String avatarURL = author.getAvatar();
                            try {
                                Glide.with(holder.getPostAuthorAvatar()).load(avatarURL).placeholder(R.drawable.default_user_profile_icon).into(holder.getPostAuthorAvatar());
                            } catch (Exception ignored) {}

                            // Если пост просматривается администратором или автором, то его можно модерировать
                            if (holder.getMenu().getMenu().size() == 0) {
                                if (currentPost.getAuthorUid().equals(userStats.getUid()) ||
                                        userStats.getUserStatus() >= UserModel.STATUS_ADMIN) {
                                    holder.getMenu().inflate(R.menu.post_author_menu);
                                } else {
                                    holder.getMenu().inflate(R.menu.post_user_menu);
                                }
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                }
        );

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(currentPost.getUploadTime());
        String time = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(calendar.getTime());

        holder.getPostDate().setText(time);
        holder.getPostTitle().setText(currentPost.getTitle());
        holder.getPostContent().setText(currentPost.getDescription());
        holder.setPostID(currentPost.getUid());

        holder.getPostOptions().setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.getMenu().show();
            }
        });

        setupAdapterWithData(holder);
    }

    // Метод инициализации картинок в посте
    private void initializeImages(ViewHolder holder, int imagesCount) {
        Context context = holder.getPostTitle().getContext();
        // Вычисляем количество изображений в ряду
        int spanCount = 1;
        if (imagesCount > 1) {
            if (imagesCount % 3 == 0 || imagesCount % 2 == 1) {
                spanCount = 3;
            } else if (imagesCount <= 4) {
                spanCount = 2;
            } else {
                spanCount = 4;
            }
        }

        holder.getImages().setLayoutManager(new GridLayoutManager(context, spanCount));
        holder.getImages().setNestedScrollingEnabled(false);
        holder.getAdapter().setOnImageClickListener(new GalleryAdapter.OnImageClickListener() {
            @Override
            public void onClick(View v, String url) {
                // Получаем данные, прогоняем их дальше
                postOnClickListener.onImageClick(url);
            }
        });
        // Установка адаптера
        holder.getImages().setAdapter(holder.getAdapter());
    }

    // Метод для создания адаптера ссылками на изображения
    private void setupAdapterWithData(ViewHolder holder) {
        String postID = holder.getPostID();
        PostModel.getNewsPostQuery(postID).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds : snapshot.getChildren()) {
                            PostModel post = ds.getValue(PostModel.class);
                            // Загружаем массив с изображениями в адаптер
                            holder.getAdapter().setDataSet(post.getImagesURLs());

                            // Инициализируем список с полученными данными
                            int imagesCount;
                            try {
                                imagesCount = post.getImagesURLs().size();
                            } catch (Exception e) {
                                imagesCount = 0;
                            }
                            initializeImages(holder, imagesCount);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }
                }
        );
    }

    // Метод для удаления постов
    private void ErasePost(String postID, Context context) {
        // Получаем ссылку на пост
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("News_posts")
                .child(postID);

        // Если это был последний пост, то список очищается
        if (dataSet.size() == 1) {
            dataSet.clear();
            notifyDataSetChanged();
        }

        // Удаляем пост
        reference.removeValue().addOnSuccessListener(
                new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        // Удаление поста завершено
                        Toast.makeText(context, context.getString(R.string.post_deleted), Toast.LENGTH_SHORT).show();
                    }
                }
        ).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(context, context.getString(R.string.post_delete_error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    // Интерфейс для взаимодействия с элементами адаптера из других активностей
    public interface PostOnClickListener {
        void onClick(String postId, int postType);
        void onAvatarClick(String userId);
        void onImageClick(String url);
        void onUpdatePost(String id, int postType);
        void onURLClick(String contestURL);
    }

    // Сеттеры для интерфейсов
    public void setPostOnClickListener(PostOnClickListener postOnClickListener) {
        this.postOnClickListener = postOnClickListener;
    }
}
