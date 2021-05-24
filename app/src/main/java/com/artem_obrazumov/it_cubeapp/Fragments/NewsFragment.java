package com.artem_obrazumov.it_cubeapp.Fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.artem_obrazumov.it_cubeapp.Activities.ImageViewerActivity;
import com.artem_obrazumov.it_cubeapp.Activities.NewPostActivity;
import com.artem_obrazumov.it_cubeapp.Activities.PostDetailActivity;
import com.artem_obrazumov.it_cubeapp.Activities.ProfileActivity;
import com.artem_obrazumov.it_cubeapp.Adapters.PostsAdapter;
import com.artem_obrazumov.it_cubeapp.Models.PostModel;
import com.artem_obrazumov.it_cubeapp.Models.UserModel;
import com.artem_obrazumov.it_cubeapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class NewsFragment extends Fragment {

    // View-элементы
    private RecyclerView newsRecyclerView;
    private LinearLayout nothing_found;
    private TextView nothing_found_text;
    private ProgressBar progressBar;

    // База данных
    private FirebaseAuth auth;
    private FirebaseDatabase database;

    // Список с постами для отображения
    private ArrayList<PostModel> posts;

    // Адаптер для списка новостей
    private PostsAdapter postsAdapter;

    // Характеристики пользователя
    private UserModel userStats;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragments_posts, container, false);

        nothing_found = root.findViewById(R.id.nothing_found_notification);
        nothing_found_text = root.findViewById(R.id.nothing_found_text);
        nothing_found_text.setText(getString(R.string.no_news_posts_found));
        progressBar = root.findViewById(R.id.progressBar);

        // Инициализация базы данных
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Инициализация RecyclerView
        posts = new ArrayList<>();
        newsRecyclerView = root.findViewById(R.id.postsRecyclerView);
        getUserStats();

        return root;
    }

    private void loadPosts() {
        DatabaseReference reference = database.getReference("News_posts");
        Query postsQuery = reference.orderByChild("cubeID").equalTo(userStats.getCubeId());
        postsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                posts.clear();
                progressBar.setVisibility(View.GONE);
                for (DataSnapshot ds : snapshot.getChildren()) {
                    PostModel post = ds.getValue(PostModel.class);
                    if ( post.getPublishType() == PostModel.STATE_PUBLISHED ) {
                        posts.add(post);
                    }
                    postsAdapter.setPostsDataSet(posts);
                }

                // Если постов нет, то показываем уведомление об этом
                if (posts.size() == 0) {
                    nothing_found.setVisibility(View.VISIBLE);
                } else {
                    nothing_found.setVisibility(View.GONE);
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), R.string.failed_loading_posts, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Получение характеристик пользователя
    private void getUserStats() {
        userStats = new UserModel();
        UserModel.getUserQuery(auth.getUid()).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            userStats = ds.getValue(UserModel.class);
                            loadPosts();
                            initializeRecyclerView();
                            initializeAdapterListenerForRecyclerView();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Не удалось получить данные пользователя, заполняем дефолтными
                        userStats.setUid("");
                        userStats.setUserStatus(0);
                    }
                }
        );
    }

    // Инициализация RecyclerView
    private void initializeRecyclerView() {
        // Создание LayoutManager для RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        // Присваиваем RecyclerView адаптер с постами и LayoutManager
        postsAdapter = new PostsAdapter(posts);
        newsRecyclerView.setLayoutManager(layoutManager);
        postsAdapter.setUserStats(userStats);
        newsRecyclerView.setAdapter(postsAdapter);

        // Создаем разделитель между постами
        DividerItemDecoration divider = new DividerItemDecoration(newsRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.line_divider));
        newsRecyclerView.addItemDecoration(divider);
    }

    // Инициализация слушателей для RecyclerView
    private void initializeAdapterListenerForRecyclerView() {
        PostsAdapter.PostOnClickListener postOnClickListener =
                new PostsAdapter.PostOnClickListener() {
                    @Override
                    public void onClick(String postId, int postType) {
                        // Нажатие на пост
                        Intent intent = new Intent(getContext(), PostDetailActivity.class);
                        intent.putExtra("PostID", postId);
                        intent.putExtra("PostType", postType);
                        startActivity(intent);
                    }

                    @Override
                    public void onAvatarClick(String userId) {
                        // Нажатие на аватарку
                        Intent intent = new Intent(getContext(), ProfileActivity.class);
                        intent.putExtra("profileUid", userId);
                        startActivity(intent);
                    }

                    @Override
                    public void onImageClick(String url) {
                        // Нажатие на изображение
                        // Переходим на другую активность и передаем ссылку на фото
                        Intent intent = new Intent(getContext(), ImageViewerActivity.class);
                        intent.putExtra("imageURL", url);
                        startActivity(intent);
                    }

                    @Override
                    public void onUpdatePost(String id, int postType) {
                        // Изменение поста
                        Intent intent = new Intent(getContext(), NewPostActivity.class);
                        intent.putExtra("mode", NewPostActivity.EDIT_POST);
                        intent.putExtra("existingPostID", id);
                        intent.putExtra("postType", postType);
                        startActivity(intent);
                    }

                    @Override
                    public void onURLClick(String contestURL) {

                    }
                };

        // Установка слушателя нажатий
        postsAdapter.setPostOnClickListener(postOnClickListener);
    }
}