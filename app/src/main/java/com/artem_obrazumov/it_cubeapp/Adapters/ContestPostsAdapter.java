package com.artem_obrazumov.it_cubeapp.Adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.artem_obrazumov.it_cubeapp.Models.ContestPostModel;
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

public class ContestPostsAdapter extends RecyclerView.Adapter<ContestPostsAdapter.ViewHolder> {

    private int currentPosition = 0;

    // Информация в адаптере
    private ArrayList<ContestPostModel> dataSet;
    private int postsType = PostModel.POST_TYPE_HACKATHON;
    // Характеристики пользователя
    private UserModel userStats;

    //Интерфейсы для обработки нажатий
    private PostsAdapter.PostOnClickListener postOnClickListener;

    // Конструкторы для адаптера
    public ContestPostsAdapter() {

    }
    public ContestPostsAdapter(ArrayList<ContestPostModel> dataSet) {
        this.dataSet = dataSet;
    }

    // Метод для обновления списка новостей
    public void setContestDataSet(ArrayList<ContestPostModel> newDataSet) {
        this.dataSet = newDataSet;
        notifyDataSetChanged();
    }

    // Метод для обновления характеристик пользователя
    public void setUserStats(UserModel userStats) {
        this.userStats = userStats;
    }

    // Изменение типа постов
    public void setPostsType(int postType) {
        this.postsType = postType;
    }

    // Класс ViewHolder
    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        Context context;

        // View-элементы
        private final ImageView post_author_avatar, preview_image;
        private final TextView post_author_name, post_date, post_title, post_content, is_opened;
        private final TextView post_options;
        private final Button participate_button;

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
            preview_image = view.findViewById(R.id.preview_image);
            is_opened = view.findViewById(R.id.is_opened);
            participate_button = view.findViewById(R.id.participate_button);
            participate_button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // Нажатие на кнопку "записаться"
                    String url = dataSet.get(getAdapterPosition()).getLink();
                    postOnClickListener.onURLClick(url);
                }
            });

            // Обработка нажатия на изображение
            preview_image.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    postOnClickListener.onImageClick(dataSet.get(getAdapterPosition()).getImagesURLs().get(0));
                }
            });

            // Инициализация меню
            context = post_author_avatar.getContext();
            menu = new PopupMenu(context, post_options);

            menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.edit_post_option:
                            // Редактирование поста
                            postOnClickListener.onUpdatePost(postID, postsType);
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

        public PopupMenu getMenu() {
            return menu;
        }

        public String getPostID() {
            return this.postID;
        }

        public ImageView getPreviewImage() {
            return preview_image;
        }

        public Button getParticipateButton() {
            return participate_button;
        }

        public TextView getIsOpenedText() {
            return is_opened;
        }

        public Context getContext() {
            return context;
        }

        // Сеттеры
        public void setPostID(String uid) {
            this.postID = uid;
        }

        // Реализация интерфейса
        @Override
        public void onClick(View v) {
            String post_id = dataSet.get(getAdapterPosition()).getUid();
            postOnClickListener.onClick(post_id, postsType);
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.contest_post_row, parent, false);

        currentPosition++;

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ContestPostModel currentPost = dataSet.get(position);
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
                            } catch (Exception e) {
                                // Если не удалось загрузить изображение с интернета, ничего не делаем
                            }

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

        checkIfOver(holder, currentPost);

        Calendar calendar = Calendar.getInstance(Locale.getDefault());
        calendar.setTimeInMillis(currentPost.getUploadTime());
        String time = new SimpleDateFormat("dd.MM.yyyy HH:mm", Locale.getDefault()).format(calendar.getTime());

        try {
            String previewImageUrl = currentPost.getImagesURLs().get(0);
            Glide.with(holder.getPostAuthorAvatar()).load(previewImageUrl).placeholder(R.color.placeholder_color).into(holder.getPreviewImage());
            holder.getPreviewImage().setVisibility(View.VISIBLE);
        } catch (Exception e) {
            // Если не удалось загрузить изображение с интернета, прячем изображение
            holder.getPreviewImage().setVisibility(View.GONE);
        }
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
    }

    // Проверяен, можно ли еще записаться на мероприятие
    private void checkIfOver(ViewHolder holder, ContestPostModel post) {
        long currentTime = Calendar.getInstance().getTimeInMillis();
        if (currentTime > post.getOpenUntil()) {
            holder.getIsOpenedText().setText(holder.getContext().getString(R.string.contestIsClosed));
            holder.getIsOpenedText().setTextColor(Color.rgb(255, 0, 0));
            holder.getParticipateButton().setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(holder.getContext(), holder.getContext().getString(R.string.contestIsClosed),
                            Toast.LENGTH_SHORT).show();
                }
            });
            holder.getParticipateButton().setAlpha(0.5f);
        }
    }

    // Метод для удаления постов
    private void ErasePost(String postID, Context context) {
        // Получаем ссылку на пост
        DatabaseReference reference;
        if (postsType == PostModel.POST_TYPE_HACKATHON) {
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
                        Toast.makeText(context, context.getString(R.string.post_deleted), Toast.LENGTH_SHORT).show();

                        // Если это был последний пост, то список очищается
                        if (dataSet.size() == 1) {
                            dataSet.clear();
                            notifyDataSetChanged();
                        }
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

    // Сеттеры для интерфейсов
    public void setPostOnClickListener(PostsAdapter.PostOnClickListener postOnClickListener) {
        this.postOnClickListener = postOnClickListener;
    }
}
