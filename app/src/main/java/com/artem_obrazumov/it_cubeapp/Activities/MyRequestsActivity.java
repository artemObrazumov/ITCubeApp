package com.artem_obrazumov.it_cubeapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.artem_obrazumov.it_cubeapp.Adapters.RequestsAdapter;
import com.artem_obrazumov.it_cubeapp.Models.PostModel;
import com.artem_obrazumov.it_cubeapp.Models.RequestModel;
import com.artem_obrazumov.it_cubeapp.R;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

public class MyRequestsActivity extends AppCompatActivity {

    // Объекты для списка
    private RecyclerView requestsList;
    private RequestsAdapter adapter;

    // Firebase
    private FirebaseDatabase database;
    private FirebaseAuth auth;

    // Окно для уведомления пользователя загрузке
    private ProgressDialog progressDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_requests);

        // Инициализация элементов
        requestsList = findViewById(R.id.requests);
        adapter = new RequestsAdapter();
        adapter.setListener(new RequestsAdapter.RequestOnClickListener() {
            @Override
            public void onUserAvatarClicked(String userID) {
                // Нажатие на аватарку пользователя
                Intent intent = new Intent(getApplicationContext(), ProfileActivity.class);
                intent.putExtra("profileUid", userID);
                startActivity(intent);
            }

            @Override
            public void onRequestAccepted(RequestModel request) {}

            @Override
            public void onRequestDenied(RequestModel request, String reason) {}

            @Override
            public void onRequestReserved(RequestModel request, String reason) {}
        });

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getString(R.string.loading));
        database = FirebaseDatabase.getInstance();
        auth = FirebaseAuth.getInstance();
        loadRequests();

        // Создание LayoutManager для RecyclerView
        LinearLayoutManager layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true);
        layoutManager.setReverseLayout(true);

        // Присваиваем RecyclerView адаптер с постами и LayoutManager
        requestsList.setLayoutManager(layoutManager);
        requestsList.setAdapter(adapter);

        // Создаем разделитель между заявками
        DividerItemDecoration divider = new DividerItemDecoration(requestsList.getContext(), DividerItemDecoration.VERTICAL);
        divider.setDrawable(ContextCompat.getDrawable(this, R.drawable.line_divider));
        requestsList.addItemDecoration(divider);

        setupToolbar(getString(R.string.my_requests));
    }

    // Загрузка запросов
    private void loadRequests() {
        ArrayList<RequestModel> requests = new ArrayList<>();
        DatabaseReference reference = database.getReference("Request_forms");
        Query postsQuery = reference.orderByChild("userID").equalTo(auth.getCurrentUser().getUid());
        postsQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                requests.clear();
                for (DataSnapshot ds : snapshot.getChildren()) {
                    RequestModel request = ds.getValue(RequestModel.class);
                    requests.add(request);
                    adapter.setDataSet(requests);
                }

                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), R.string.failed_loading_posts, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Установка верхней панели
    private void setupToolbar(String title) {
        try {
            getActionBar().setTitle(title);
            getActionBar().setDisplayHomeAsUpEnabled(true);
        } catch (Exception e) {
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    // Обработка нажатий на кнопки верхней панели
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        onBackPressed();
        return super.onOptionsItemSelected(item);
    }
}