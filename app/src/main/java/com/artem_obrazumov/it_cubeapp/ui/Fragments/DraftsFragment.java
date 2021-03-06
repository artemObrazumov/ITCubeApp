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

import com.artem_obrazumov.it_cubeapp.databinding.FragmentDraftsBinding;
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
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class DraftsFragment extends Fragment {

    // Binding
    private FragmentDraftsBinding binding;

    // Firebase
    private FirebaseAuth auth;
    private FirebaseDatabase database;

    // Список с постами для отображения
    private ArrayList<PostModel> posts;

    // Адаптеры для списков постов
    private PostsAdapter postsAdapter;
    private ContestPostsAdapter contestPostsAdapter;

    // Характеристики пользователя
    private UserModel userStats;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentDraftsBinding.inflate(inflater);
        View root = binding.getRoot();

        // Инициализация элементов
        initializeRecyclerView();

        binding.tabs.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                switch (tab.getPosition()) {
                    case 0:
                        loadNewsPosts();
                        break;
                    case 1:
                        loadHackathonPosts();
                        break;
                    case 2:
                        loadContestPosts();
                        break;
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        postsAdapter = new PostsAdapter();
        contestPostsAdapter = new ContestPostsAdapter();
        initializeAdapterListeners();

        auth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        getUserStats();

        return root;
    }

    // Инициализация слушателей для адаптеров
    private void initializeAdapterListeners() {
        PostsAdapter.PostOnClickListener listener = new PostsAdapter.PostOnClickListener() {
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
                // Переход по ссылке для участия
                try {
                    Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(contestURL));
                    startActivity(browserIntent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), getString(R.string.link_not_working), Toast.LENGTH_SHORT).show();
                }
            }
        };

        postsAdapter.setPostOnClickListener(listener);
        contestPostsAdapter.setPostOnClickListener(listener);
    }

    // Загрузка данных пользователя
    private void getUserStats() {
        userStats = new UserModel();
        UserModel.getUserQuery(auth.getUid()).addListenerForSingleValueEvent(
                new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot ds: snapshot.getChildren()) {
                            userStats = ds.getValue(UserModel.class);
                            postsAdapter.setUserStats(userStats);
                            contestPostsAdapter.setUserStats(userStats);

                            loadNewsPosts();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        // Не удалось получить данные пользователя, заполняем дефолтными
                        userStats.setUid("");
                        userStats.setUserStatus(0);
                        postsAdapter.setUserStats(userStats);
                        contestPostsAdapter.setUserStats(userStats);

                        loadNewsPosts();
                    }
                });
    }

    // Инициализация RecyclerView
    private void initializeRecyclerView() {
        // Создание LayoutManager для RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity());
        layoutManager.setStackFromEnd(true);

        // Присваиваем RecyclerView адаптер с постами и LayoutManager
        binding.draftsPostsList.setLayoutManager(layoutManager);
        binding.draftsPostsList.setAdapter(postsAdapter);
        binding.draftsPostsList.setNestedScrollingEnabled(true);

        // Создаем разделитель между постами
        DividerItemDecoration divider = new DividerItemDecoration(binding.draftsPostsList.getContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(getContext(), R.drawable.line_divider));
        binding.draftsPostsList.addItemDecoration(divider);
    }

    // Загрузка всех черновиков - новостных постов пользователя
    public void loadNewsPosts() {
        binding.progressBar.setVisibility(View.VISIBLE);
        ArrayList<PostModel> posts = new ArrayList<>();
        postsAdapter.setPostsDataSet(posts);
        binding.draftsPostsList.setAdapter(postsAdapter);

        DatabaseReference reference = database.getReference("News_posts");
        Query postsQuery = reference.orderByChild("authorUid").equalTo(auth.getCurrentUser().getUid());
        postsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                posts.clear();
                for (DataSnapshot ds: snapshot.getChildren()) {
                    PostModel post = ds.getValue(PostModel.class);
                    if (post.getPublishType() == PostModel.STATE_DRAFT) {
                        posts.add(post);
                    }
                }
                postsAdapter.setPostsDataSet(posts);

                binding.progressBar.setVisibility(View.GONE);
                if (posts.size() == 0) {
                    binding.messageLayout.setVisibility(View.VISIBLE);
                    binding.nothingFoundText.setText(getString(R.string.no_draft_news_post));
                } else {
                    binding.messageLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Загрузка всех черновиков - хакатонских постов пользователя
    public void loadHackathonPosts() {
        binding.progressBar.setVisibility(View.VISIBLE);
        ArrayList<ContestPostModel> posts = new ArrayList<>();
        contestPostsAdapter.setContestDataSet(posts);
        contestPostsAdapter.setPostsType(PostModel.POST_TYPE_HACKATHON);
        binding.draftsPostsList.setAdapter(contestPostsAdapter);

        DatabaseReference reference = database.getReference("Hackathon_posts");
        Query postsQuery = reference.orderByChild("authorUid").equalTo(auth.getCurrentUser().getUid());
        postsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    posts.clear();
                    ContestPostModel post = ds.getValue(ContestPostModel.class);
                    if (post.getPublishType() == PostModel.STATE_DRAFT) {
                        posts.add(post);
                    }
                }
                contestPostsAdapter.setContestDataSet(posts);

                binding.progressBar.setVisibility(View.GONE);
                if (posts.size() == 0) {
                    binding.messageLayout.setVisibility(View.VISIBLE);
                    binding.nothingFoundText.setText(getString(R.string.no_draft_hackathon_post));
                } else {
                    binding.messageLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Загрузка всех черновиков - конкурсных постов пользователя
    public void loadContestPosts() {
        binding.progressBar.setVisibility(View.VISIBLE);
        ArrayList<ContestPostModel> posts = new ArrayList<>();
        contestPostsAdapter.setContestDataSet(posts);
        contestPostsAdapter.setPostsType(PostModel.POST_TYPE_CONTEST);
        binding.draftsPostsList.setAdapter(contestPostsAdapter);

        DatabaseReference reference = database.getReference("Contest_posts");
        Query postsQuery = reference.orderByChild("authorUid").equalTo(auth.getCurrentUser().getUid());
        postsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot ds: snapshot.getChildren()) {
                    posts.clear();
                    ContestPostModel post = ds.getValue(ContestPostModel.class);
                    if (post.getPublishType() == PostModel.STATE_DRAFT) {
                        posts.add(post);
                    }
                }
                contestPostsAdapter.setContestDataSet(posts);

                binding.progressBar.setVisibility(View.GONE);
                if (posts.size() == 0) {
                    binding.messageLayout.setVisibility(View.VISIBLE);
                    binding.nothingFoundText.setText(getString(R.string.no_draft_contest_post));
                } else {
                    binding.messageLayout.setVisibility(View.GONE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), getString(R.string.error), Toast.LENGTH_SHORT).show();
            }
        });
    }


}