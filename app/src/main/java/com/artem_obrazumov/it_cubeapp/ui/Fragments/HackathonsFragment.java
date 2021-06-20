package com.artem_obrazumov.it_cubeapp.ui.Fragments;

import android.content.Intent;
import android.net.Uri;
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

import com.artem_obrazumov.it_cubeapp.databinding.FragmentsPostsBinding;
import com.artem_obrazumov.it_cubeapp.ui.Activities.ImageViewerActivity;
import com.artem_obrazumov.it_cubeapp.ui.Activities.NewPostActivity;
import com.artem_obrazumov.it_cubeapp.ui.Activities.PostDetailActivity;
import com.artem_obrazumov.it_cubeapp.ui.Activities.ProfileActivity;
import com.artem_obrazumov.it_cubeapp.Adapters.ContestPostsAdapter;
import com.artem_obrazumov.it_cubeapp.Adapters.PostsAdapter;
import com.artem_obrazumov.it_cubeapp.Models.ContestPostModel;
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

public class HackathonsFragment extends Fragment {

    // Binding
    private FragmentsPostsBinding binding;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseDatabase database;

    // Список с постами для отображения
    private ArrayList<ContestPostModel> hackathonsPosts;

    // Адаптер для списка новостей
    private ContestPostsAdapter contestPostsAdapter;

    // Характеристики пользователя
    private UserModel userStats;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentsPostsBinding.inflate(inflater);
        View root = binding.getRoot();

        // Инициализация базы данных
        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();

        // Инициализация RecyclerView
        hackathonsPosts = new ArrayList<>();
        getUserStats();

        return root;
    }

    private void loadPosts() {
        DatabaseReference reference = database.getReference("Hackathon_posts");
        Query postsQuery = reference.orderByChild("cubeID").equalTo(userStats.getCubeId());
        postsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                hackathonsPosts.clear();
                binding.progressBar.setVisibility(View.GONE);
                for (DataSnapshot ds : snapshot.getChildren()) {
                    ContestPostModel post = ds.getValue(ContestPostModel.class);
                    if ( post.getPublishType() != PostModel.STATE_DRAFT ) {
                        hackathonsPosts.add(post);
                    }
                    contestPostsAdapter.setContestDataSet(hackathonsPosts);
                }

                // Если постов нет, то показываем уведомление об этом
                if (hackathonsPosts.size() == 0) {
                    binding.nothingFoundNotification.setVisibility(View.VISIBLE);
                    binding.nothingFoundText.setText(getString(R.string.no_hackathon_posts_found));
                } else {
                    binding.nothingFoundNotification.setVisibility(View.GONE);
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
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        // Присваиваем RecyclerView адаптер с постами и LayoutManager
        contestPostsAdapter = new ContestPostsAdapter(hackathonsPosts);
        contestPostsAdapter.setPostsType(PostModel.POST_TYPE_HACKATHON);
        binding.postsRecyclerView.setLayoutManager(layoutManager);
        contestPostsAdapter.setUserStats(userStats);
        binding.postsRecyclerView.setAdapter(contestPostsAdapter);

        // Создаем разделитель между постами
        DividerItemDecoration divider = new DividerItemDecoration(binding.postsRecyclerView.getContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.line_divider));
        binding.postsRecyclerView.addItemDecoration(divider);
    }

    // Инициализация адаптеров для RecyclerView
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
                        try {
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(contestURL));
                            startActivity(browserIntent);
                        } catch (Exception e) {
                            Toast.makeText(getContext(), getString(R.string.link_not_working), Toast.LENGTH_SHORT).show();
                        }
                    }
                };

        // Установка слушателя нажатий
        contestPostsAdapter.setPostOnClickListener(postOnClickListener);
    }
}